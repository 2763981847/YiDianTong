package com.oreki.yygh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.cmn.client.DictFeignClient;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.mapper.HospitalSetMapper;
import com.oreki.yygh.model.hosp.*;
import com.oreki.yygh.repository.ScheduleRepository;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.hosp.BookingScheduleRuleVo;
import com.oreki.yygh.vo.hosp.ScheduleOrderVo;
import com.oreki.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Resource
    private ScheduleRepository scheduleRepository;
    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private MongoTemplate mongoTemplate;


    /**
     * 上传排班
     *
     * @param paramMap 参数表
     */
    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        //将map中的参数转为实体对象
        String jsonString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSON.parseObject(jsonString, Schedule.class);
        //判断数据库中是否含有该数据
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        //存在数据则进行修改，不存在则进行新增
        if (scheduleExist == null) {
            schedule.setCreateTime(new Date());
            schedule.setStatus(1);
        } else {
            schedule.setId(scheduleExist.getId());
            schedule.setStatus(scheduleExist.getStatus());
            schedule.setCreateTime(scheduleExist.getCreateTime());
        }
        schedule.setUpdateTime(new Date());
        schedule.setIsDeleted(0);
        scheduleRepository.save(schedule);
    }

    /**
     * 查询排班
     *
     * @param page            当前页
     * @param limit           每页数据量
     * @param scheduleQueryVo 查询条件
     * @return 查到的分页对象
     */
    @Override
    public Page<Schedule> getSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //创建pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //创建example对象
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        //分页查询
        Page<Schedule> pageModel = scheduleRepository.findAll(example, pageable);
        return pageModel;
    }

    /**
     * 删除排班接口
     *
     * @param hoscode       医院编码
     * @param hosScheduleId 排班id
     */
    @Override
    public void removeSchedule(String hoscode, String hosScheduleId) {
        scheduleRepository.deleteByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
    }

    /**
     * 根据医院编号和科室编号查看排班
     *
     * @param page    当前页
     * @param limit   每页数据量
     * @param hoscode 医院编码
     * @param depcode 科室编码
     * @return 查询到的分页对象
     */
    @Override
    public Map<String, Object> listSchedules(int page, int limit, String hoscode, String depcode) {
        //获取到所有排班信息
        List<Schedule> schedules = scheduleRepository.getSchedulesByHoscodeAndDepcode(hoscode, depcode);
        //设置total
        int total = schedules.stream().collect(Collectors.groupingBy(Schedule::getWorkDate)).size();
        //构建BookingScheduleRuleVoList
        List<BookingScheduleRuleVo> list = buildBookingScheduleRuleVoList(page, limit, schedules);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //获取医院名称
        String hosName = hospitalService.getByHoscode(hoscode).getHosname();
        baseMap.put("hosname", hosName);
        //封装返回结果数据
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("bookingScheduleList", list);
        result.put("baseMap", baseMap);
        return result;
    }

    /**
     * 根据医院编号和科室编号和日期查看排班详细信息
     *
     * @param hoscode  医院编码
     * @param depcode  科室编码
     * @param workDate 排班日期
     * @return 排班详细信息列表
     */
    @Override
    public List<Schedule> listScheduleDetails(String hoscode, String depcode, Date workDate) {
        //查询到所有排班
        List<Schedule> schedules = scheduleRepository.getSchedulesByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, workDate);
        //再封装医院名称，科室名称，周几的信息
        schedules.forEach(this::packageSchedule);
        return schedules;
    }

    /**
     * 获取可预约的排版数据
     *
     * @param page    当前页
     * @param limit   每页记录数
     * @param hoscode 医院编码
     * @param depcode 科室编码
     * @return 可预约的排班数据
     */
    @Override
    public Map<String, Object> getBookingSchedulePage(Integer page, Integer limit, String hoscode, String depcode) {
        HashMap<String, Object> result = new HashMap<>();
        //根据医院编号获取到预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期的分页数据
        Page<Date> datePage = getDatePage(page, limit, bookingRule);
        List<Date> dateList = datePage.getContent();
        //获取可预约日期科室剩余预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();
        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && page == datePage.getTotalPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", datePage.getTotalElements());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentService.getByHoscodeAndDepcode(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    /**
     * 根据id获取排班信息
     *
     * @param id 排班ID
     * @return 排班信息
     */
    @Override
    public Schedule getScheduleById(String id) {
        Schedule schedule = scheduleRepository.getScheduleById(id);
        this.packageSchedule(schedule);
        return schedule;
    }

    /**
     * 根据排班id获取预约下单数据
     *
     * @param scheduleId 排班ID
     * @return 下单数据
     */
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //根据id查到排班数据
        Schedule schedule = this.getScheduleById(scheduleId);
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //向scheduleOrderVo中设置信息
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;
    }

    /**
     * 用于mq的排班信息更新
     *
     * @param schedule 排班信息
     */
    @Override
    public void mqUpdate(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    /**
     * 获取可预约日期的分页数据
     *
     * @param page        当前页
     * @param limit       每页记录数
     * @param bookingRule 排版规则
     * @return 分页数据
     */
    private Page<Date> getDatePage(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天的放号时间
        DateTime releaseDateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //获取预约周期
        int cycle = bookingRule.getCycle();
        //如果超过了今天的放号时间，预约周期从明天开始算
        if (releaseDateTime.isBeforeNow()) {
            cycle++;
        }
        //获取所有可预约日期，最后一天显示即将放号
        List<Date> dateList = new LinkedList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime dateTime = new DateTime().plusDays(i);
            Date date = new DateTime(dateTime.toString("yyyy-MM-dd")).toDate();
            dateList.add(date);
        }
        //构建分页内容
        List<Date> pageDateList = dateList.subList((page - 1) * limit, Math.min(dateList.size(), page * limit));
        //构建分页
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Date> datePage = new PageImpl<>(pageDateList, pageable, dateList.size());
        return datePage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     *
     * @param date       年月日
     * @param timeString 时分
     * @return 转换后的日期
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }


    /**
     * 封装排班信息的其他属性
     *
     * @param schedule 排班信息
     */
    private void packageSchedule(Schedule schedule) {
        Map<String, Object> param = schedule.getParam();
        //设置医院名称
        param.put("hosname", hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        //设置科室名称
        param.put("depname", departmentService.getByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        //设置周几
        param.put("dayOfWeek", getDayOfWeek(new DateTime(schedule.getWorkDate())));
        schedule.setParam(param);
    }

    private List<BookingScheduleRuleVo> buildBookingScheduleRuleVoList(int page, int limit, List<Schedule> schedules) {
        //根据日期分组
        Map<Date, List<Schedule>> dateListMap = schedules.stream().collect(Collectors.groupingBy(Schedule::getWorkDate));
        //构建BookingScheduleRuleVoList
        List<BookingScheduleRuleVo> list = new LinkedList<>();
        dateListMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .skip((long) (page - 1) * limit).limit(limit).forEach(entry -> {
                    BookingScheduleRuleVo bookingScheduleRuleVo = new BookingScheduleRuleVo();
                    Date workDate = entry.getKey();
                    List<Schedule> scheduleList = entry.getValue();
                    //统计就诊医生人数
                    int docCount = (int) scheduleList.stream().map(Schedule::getDocname).distinct().count();
                    //统计科室可预约数和剩余预约数
                    int reservedNumber = (int) scheduleList.stream().mapToLong(Schedule::getReservedNumber).sum();
                    int availableNumber = (int) scheduleList.stream().mapToLong(Schedule::getAvailableNumber).sum();
                    //根据日期获取周几
                    String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
                    //将数据进行封装并加入列表
                    bookingScheduleRuleVo.setWorkDate(workDate);
                    bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
                    bookingScheduleRuleVo.setDocCount(docCount);
                    bookingScheduleRuleVo.setReservedNumber(reservedNumber);
                    bookingScheduleRuleVo.setAvailableNumber(availableNumber);
                    list.add(bookingScheduleRuleVo);
                });
        return list;
    }

    /**
     * 根据日期获取周几
     *
     * @param dateTime 日期
     * @return 周几
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
