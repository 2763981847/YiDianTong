package com.oreki.yygh.order.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.enums.RefundStatusEnum;
import com.oreki.yygh.model.order.PaymentInfo;
import com.oreki.yygh.model.order.RefundInfo;
import com.oreki.yygh.order.mapper.RefundInfoMapper;
import com.oreki.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 27639
 * @description 针对表【refund_info(退款信息表)】的数据库操作Service实现
 * @createDate 2022-10-18 20:56:05
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo>
        implements RefundInfoService {
    /**
     * 保存退款记录
     *
     * @param paymentInfo 支付记录
     * @return 退款记录
     */
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //判断是否已有记录
        LambdaQueryWrapper<RefundInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RefundInfo::getOrderId, paymentInfo.getOrderId())
                .eq(RefundInfo::getPaymentType, paymentInfo.getPaymentType());
        RefundInfo refundInfo = this.getOne(queryWrapper);
        if (refundInfo != null) return refundInfo;
        // 保存交易记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        this.save(refundInfo);
        return refundInfo;
    }
}




