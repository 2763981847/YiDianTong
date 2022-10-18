package com.oreki.yygh.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.model.order.PaymentInfo;

import java.util.Map;

/**
* @author 27639
* @description 针对表【payment_info(支付信息表)】的数据库操作Service
* @createDate 2022-10-18 10:57:52
*/
public interface PaymentInfoService extends IService<PaymentInfo> {

    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    void paySuccess(String outTradeNo, Integer status, Map<String, String> resultMap);
}
