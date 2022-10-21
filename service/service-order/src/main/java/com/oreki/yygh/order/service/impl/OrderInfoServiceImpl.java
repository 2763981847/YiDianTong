package com.oreki.yygh.order.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.common.rabbit.constant.MQConstant;
import com.oreki.common.rabbit.service.RabbitService;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.helper.HttpRequestHelper;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.enums.OrderStatusEnum;
import com.oreki.yygh.hosp.client.HospitalFeignClient;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.model.user.Patient;
import com.oreki.yygh.order.service.OrderInfoService;
import com.oreki.yygh.order.mapper.OrderInfoMapper;
import com.oreki.yygh.order.service.WeixinService;
import com.oreki.yygh.user.client.UserFeignClient;
import com.oreki.yygh.vo.hosp.ScheduleOrderVo;
import com.oreki.yygh.vo.msm.MsmVo;
import com.oreki.yygh.vo.order.*;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author 27639
 * @description 针对表【order_info(订单表)】的数据库操作Service实现
 * @createDate 2022-10-15 11:20:25
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

    @Resource
    private WeixinService weixinService;
    @Resource
    private UserFeignClient userFeignClient;
    @Resource
    private HospitalFeignClient hospitalFeignClient;
    @Resource
    private RabbitService rabbitService;


    /**
     * 提交订单并返回订单id
     *
     * @param scheduleId 排班id
     * @param patientId  就诊人id
     * @return 订单id
     */
    @Override
    public long saveOrder(String scheduleId, Long patientId) {
        //获取预约下单数据
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        //判断当前时间是否可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow() || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        //获取就诊人信息
        Patient patient = userFeignClient.getPatientOrder(patientId);
        //获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        //添加信息到orderInfo
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        //添加其他信息到orderInfo
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setHosScheduleId(scheduleOrderVo.getHosScheduleId());
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);
        //设置调用医院接口需要的参数到map中
        Map<String, Object> paramMap = getParamMap(patient, signInfoVo, orderInfo);
        //请求医院系统接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");
        if (result.getIntValue("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        JSONObject jsonObject = result.getJSONObject("data");
        //预约记录唯一标识（医院预约记录主键）
        String hosRecordId = jsonObject.getString("hosRecordId");
        //预约序号
        Integer number = jsonObject.getInteger("number");
        //取号时间
        String fetchTime = jsonObject.getString("fetchTime");
        //取号地址
        String fetchAddress = jsonObject.getString("fetchAddress");
        //更新订单
        orderInfo.setHosRecordId(hosRecordId);
        orderInfo.setNumber(number);
        orderInfo.setFetchTime(fetchTime);
        orderInfo.setFetchAddress(fetchAddress);
        baseMapper.updateById(orderInfo);
        //排班可预约数
        Integer reservedNumber = jsonObject.getInteger("reservedNumber");
        //排班剩余预约数
        Integer availableNumber = jsonObject.getInteger("availableNumber");
        //号源更新
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setAvailableNumber(availableNumber);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setScheduleId(scheduleId);
        //短信发送
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(patient.getPhone());
        String reserveDate =
                new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                        + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
            put("amount", orderInfo.getAmount());
            put("reserveDate", reserveDate);
            put("name", orderInfo.getPatientName());
            put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            put("code", "book");
        }};
        msmVo.setParam(param);
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MQConstant.EXCHANGE_DIRECT_ORDER, MQConstant.ROUTING_ORDER, orderMqVo);

        //返回订单id
        return orderInfo.getId();
    }

    /**
     * @param id
     * @return
     */
    @Override
    public OrderInfo getOrderDetails(Long id) {
        OrderInfo orderInfo = this.getById(id);
        this.packageOrder(orderInfo);
        return orderInfo;
    }

    @Override
    public Page<OrderInfo> getOrdersPage(int page, int limit, OrderQueryVo orderQueryVo) {
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        //取出查询条件
        Long userId = orderQueryVo.getUserId();
        String orderStatus = orderQueryVo.getOrderStatus();
        String patientName = orderQueryVo.getPatientName();
        Long patientId = orderQueryVo.getPatientId();
        String keyword = orderQueryVo.getKeyword();      //医院名称
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        String reserveDate = orderQueryVo.getReserveDate();
        //构建查询wrapper
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, OrderInfo::getUserId, userId)
                .eq(!StringUtils.isEmpty(patientId), OrderInfo::getPatientId, patientId)
                .eq(!StringUtils.isEmpty(orderStatus), OrderInfo::getOrderStatus, orderStatus)
                .like(!StringUtils.isEmpty(patientName), OrderInfo::getPatientName, patientName)
                .like(!StringUtils.isEmpty(keyword), OrderInfo::getHosname, keyword)
                .ge(!StringUtils.isEmpty(createTimeBegin), OrderInfo::getCreateTime, createTimeBegin)
                .le(!StringUtils.isEmpty(createTimeEnd), OrderInfo::getCreateTime, createTimeEnd)
                .eq(!StringUtils.isEmpty(reserveDate), OrderInfo::getReserveDate, reserveDate)
                .orderByAsc(OrderInfo::getReserveDate);
        this.page(orderInfoPage, queryWrapper);
        //将订单状态进行包装
        orderInfoPage.getRecords().forEach(this::packageOrder);
        return orderInfoPage;
    }

    /**
     * 取消预约
     *
     * @param orderId 订单id
     * @return 是否取消成功
     */
    @Override
    public boolean cancelOrder(Long orderId) {
        //获取到该订单
        OrderInfo orderInfo = this.getById(orderId);
        //判断就诊时间是否已过
        if (new DateTime(orderInfo.getReserveDate()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> reqMap = getReqMap(orderInfo, signInfoVo);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            //是否支付 退款
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                //已支付 退款
                boolean isRefund = weixinService.refund(orderId);
                if (!isRefund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
            //更改订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(orderInfo);
            //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MQConstant.EXCHANGE_DIRECT_ORDER, MQConstant.ROUTING_ORDER, orderMqVo);
            return true;
        }
    }

    public Map<String, Object> getReqMap(OrderInfo orderInfo, SignInfoVo signInfoVo) {
        if (null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        return reqMap;
    }

    /**
     * 实现预约统计
     *
     * @param orderCountQueryVo 预约条件查询
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        //查询数据库
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);
        //获取x轴数据
        List<String> dateList = new LinkedList<>();
        //获取y轴数据
        List<Integer> countList = new LinkedList<>();
        for (OrderCountVo orderCountVo : orderCountVoList) {
            dateList.add(orderCountVo.getReserveDate());
            countList.add(orderCountVo.getCount());
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }

    /**
     * 将订单状态文字信息打包到返回数据中
     *
     * @param orderInfo 订单信息
     */
    private void packageOrder(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
    }

    /**
     * 设置调用医院接口需要的参数到map中
     *
     * @param patient    就诊人
     * @param signInfoVo 签名信息
     * @param orderInfo  订单信息
     * @return 参数设置
     */
    private static Map<String, Object> getParamMap(Patient patient, SignInfoVo signInfoVo, OrderInfo orderInfo) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", orderInfo.getHoscode());
        paramMap.put("depcode", orderInfo.getDepcode());
        paramMap.put("hosScheduleId", orderInfo.getHosScheduleId());
        paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount", orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        return paramMap;
    }
}




