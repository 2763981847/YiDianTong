package com.oreki.yygh.order.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.helper.HttpRequestHelper;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.enums.OrderStatusEnum;
import com.oreki.yygh.enums.PaymentStatusEnum;
import com.oreki.yygh.enums.PaymentTypeEnum;
import com.oreki.yygh.hosp.client.HospitalFeignClient;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.model.order.PaymentInfo;
import com.oreki.yygh.order.mapper.PaymentInfoMapper;
import com.oreki.yygh.order.service.OrderInfoService;
import com.oreki.yygh.order.service.PaymentInfoService;
import com.oreki.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 27639
 * @description 针对表【payment_info(支付信息表)】的数据库操作Service实现
 * @createDate 2022-10-18 10:57:52
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
        implements PaymentInfoService {

    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private HospitalFeignClient hospitalFeignClient;

    /**
     * 根据订单信息和支付方式向订单记录表中添加记录
     *
     * @param orderInfo   订单信息
     * @param paymentType 支付方式
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //判断此订单记录是否已存在，不存在才新增
        Long orderId = orderInfo.getId();
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, orderId)
                .eq(PaymentInfo::getPaymentType, paymentType);
        PaymentInfo paymentInfo = this.getOne(queryWrapper);
        //不存在则新增
        if (paymentInfo == null) {
            paymentInfo = new PaymentInfo();
            paymentInfo.setCreateTime(new Date());
            paymentInfo.setOrderId(orderInfo.getId());
            paymentInfo.setPaymentType(paymentType);
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
            String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|" + orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle();
            paymentInfo.setSubject(subject);
            paymentInfo.setTotalAmount(orderInfo.getAmount());
            this.save(paymentInfo);
        }
    }

    /**
     * 支付成功后更新表数据
     *
     * @param outTradeNo  订单编号
     * @param PaymentType 支付类型
     * @param paramMap    支付结果集
     */
    @Override
    public void paySuccess(String outTradeNo, Integer PaymentType, Map<String, String> paramMap) {
        //更新支付记录表中数据
        LambdaUpdateWrapper<PaymentInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentInfo::getOutTradeNo, outTradeNo)
                .eq(PaymentInfo::getPaymentType, PaymentType)
                .set(PaymentInfo::getPaymentStatus, PaymentStatusEnum.PAID.getStatus())
                .set(PaymentInfo::getTradeNo, paramMap.get("transaction_id"))
                .set(PaymentInfo::getCallbackTime, new Date())
                .set(PaymentInfo::getCallbackContent, paramMap.toString());
        this.update(updateWrapper);
        //更新订单表中的数据
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getOutTradeNo, outTradeNo);
        OrderInfo orderInfo = orderInfoService.getOne(queryWrapper);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        //调用医院接口，更新订单支付状态
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> reqMap = orderInfoService.getReqMap(orderInfo, signInfoVo);
        HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
    }

    /**
     * 根据订单id和支付类型获取支付记录
     *
     * @param orderId     订单id
     * @param paymentType 支付类型
     * @return 支付记录
     */
    @Override
    public PaymentInfo getPaymentInfo(long orderId, Integer paymentType) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, orderId)
                .eq(PaymentInfo::getPaymentType, paymentType);
        return this.getOne(queryWrapper);
    }
}




