package com.oreki.yygh.order.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.vo.order.OrderCountQueryVo;
import com.oreki.yygh.vo.order.OrderQueryVo;
import com.oreki.yygh.vo.order.SignInfoVo;

import java.util.Map;

/**
 * @author 27639
 * @description 针对表【order_info(订单表)】的数据库操作Service
 * @createDate 2022-10-15 11:20:25
 */
public interface OrderInfoService extends IService<OrderInfo> {

    long saveOrder(String scheduleId, Long patientId);

    OrderInfo getOrderDetails(Long id);

    Page<OrderInfo> getOrdersPage(int page, int limit, OrderQueryVo orderQueryVo);

    boolean cancelOrder(Long orderId);

    Map<String, Object> getReqMap(OrderInfo orderInfo, SignInfoVo signInfoVo);

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
