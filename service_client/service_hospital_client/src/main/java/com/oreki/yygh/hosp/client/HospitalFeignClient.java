package com.oreki.yygh.hosp.client;

import com.oreki.yygh.model.user.Patient;
import com.oreki.yygh.vo.hosp.ScheduleOrderVo;
import com.oreki.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/15 16:01
 */
@FeignClient("service-hosp")
public interface HospitalFeignClient {
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    @ApiOperation("获取医院签名信息")
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}
