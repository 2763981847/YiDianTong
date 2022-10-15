package com.oreki.yygh.order.service.impl;


import com.alibaba.fastjson.JSONObject;
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
import com.oreki.yygh.user.client.UserFeignClient;
import com.oreki.yygh.vo.hosp.ScheduleOrderVo;
import com.oreki.yygh.vo.msm.MsmVo;
import com.oreki.yygh.vo.order.OrderMqVo;
import com.oreki.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author 27639
 * @description 针对表【order_info(订单表)】的数据库操作Service实现
 * @createDate 2022-10-15 11:20:25
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {

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
        orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
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
        // todo 发送mq信息更新号源和短信通知
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
        paramMap.put("hosScheduleId", orderInfo.getScheduleId());
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




