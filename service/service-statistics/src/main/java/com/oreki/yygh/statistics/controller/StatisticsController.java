package com.oreki.yygh.statistics.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.order.client.OrderFeignClient;
import com.oreki.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/20 17:16
 */
@Api(tags = "数据统计接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
    @Resource
    private OrderFeignClient orderFeignClient;

    @ApiOperation("获取预约挂号数据统计")
    @GetMapping("/getCountMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
        return Result.ok(orderFeignClient.getCountMap(orderCountQueryVo));
    }
}
