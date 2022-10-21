package com.oreki.yygh.order.client;

import com.oreki.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/20 17:07
 */
@FeignClient("service-order")
public interface OrderFeignClient {
    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("/api/order/orderInfo/inner/getCountMap")
    Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
