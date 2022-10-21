package com.oreki.yygh.statistics.config;

import com.oreki.yygh.order.client.OrderFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/20 17:13
 */
@Configuration
@ComponentScan(basePackages = "com.oreki")
@EnableDiscoveryClient
@EnableFeignClients(clients = {OrderFeignClient.class})
@MapperScan("com.oreki.yygh.statistics.mapper")
public class StatisticsConfig {
}
