package com.oreki.yygh.order.controller.api;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.order.service.OrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

}
