package com.oreki.yygh.user.client;

import com.oreki.yygh.model.user.Patient;
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
@FeignClient("service-user")
public interface UserFeignClient {
    @ApiOperation(value = "根据id获取就诊人信息")
    @GetMapping("/api/user/patient/inner/{id}")
    Patient getPatientOrder(@PathVariable("id") Long id);
}

