package com.oreki.yygh.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/9 17:02
 */
@Configuration
@ComponentScan(basePackages = "com.oreki")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oreki")
@MapperScan("com.oreki.yygh.mapper")
public class UserConfig {
}
