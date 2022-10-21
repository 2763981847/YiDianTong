package com.oreki.yygh.order.controller.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.common.util.AuthContextHolder;
import com.oreki.yygh.enums.OrderStatusEnum;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.order.service.OrderInfoService;
import com.oreki.yygh.vo.order.OrderCountQueryVo;
import com.oreki.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/15 11:30
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("api/order/orderInfo")
public class OrderApiController {
    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId,
            @ApiParam(name = "patientId", value = "就诊人id", required = true)
            @PathVariable Long patientId) {
        return Result.ok(orderInfoService.saveOrder(scheduleId, patientId));
    }

    @ApiOperation("根据订单id查询订单详情")
    @GetMapping("auth/{id}")
    public Result getById(@PathVariable Long id) {
        OrderInfo orderInfo = orderInfoService.getOrderDetails(id);
        return Result.ok(orderInfo);
    }

    @ApiOperation("条件查询订单分页")
    @GetMapping("auth/{page}/{limit}")
    public Result getOrdersPage(@PathVariable int page, @PathVariable int limit, OrderQueryVo orderQueryVo, HttpServletRequest request) {
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> orderInfoPage = orderInfoService.getOrdersPage(page, limit, orderQueryVo);
        return Result.ok(orderInfoPage);
    }

    @ApiOperation("获取所以订单状态")
    @GetMapping("auth/orderStatus")
    public Result listOrderStatus() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation("取消预约")
    @PutMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        boolean isCanceled = orderInfoService.cancelOrder(orderId);
        return Result.judge(isCanceled);
    }

    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderInfoService.getCountMap(orderCountQueryVo);
    }


}
