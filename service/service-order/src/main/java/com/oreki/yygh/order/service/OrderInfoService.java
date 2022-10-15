package com.oreki.yygh.order.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.order.OrderInfo;

/**
* @author 27639
* @description 针对表【order_info(订单表)】的数据库操作Service
* @createDate 2022-10-15 11:20:25
*/
public interface OrderInfoService extends IService<OrderInfo> {

    long saveOrder(String scheduleId, Long patientId);
}
