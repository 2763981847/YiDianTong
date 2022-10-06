package com.oreki.yygh.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.oreki.yygh.cmn.client.DictFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.oreki.yygh.mapper")
@ComponentScan(basePackages = {"com.oreki"})
@EnableDiscoveryClient
@EnableFeignClients(clients = DictFeignClient.class)
public class HospConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
