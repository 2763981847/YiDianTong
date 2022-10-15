package com.oreki.yygh.order.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.oreki.yygh.hosp.client.HospitalFeignClient;
import com.oreki.yygh.user.client.UserFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/15 11:33
 */
@Configuration
@ComponentScan(basePackages = "com.oreki")
@EnableDiscoveryClient
@EnableFeignClients(clients = {UserFeignClient.class, HospitalFeignClient.class})
@MapperScan("com.oreki.yygh.order.mapper")
public class OrderConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
