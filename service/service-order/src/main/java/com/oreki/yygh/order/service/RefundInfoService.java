package com.oreki.yygh.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.order.PaymentInfo;
import com.oreki.yygh.model.order.RefundInfo;

/**
* @author 27639
* @description 针对表【refund_info(退款信息表)】的数据库操作Service
* @createDate 2022-10-18 20:56:05
*/
public interface RefundInfoService extends IService<RefundInfo> {
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
