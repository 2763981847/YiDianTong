package com.oreki.yygh.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oreki.yygh.model.hosp.Schedule;
import com.oreki.yygh.repository.ScheduleRepository;
import com.oreki.yygh.service.DepartmentService;
import com.oreki.yygh.service.HospitalService;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.hosp.BookingScheduleRuleVo;
import com.oreki.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

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
