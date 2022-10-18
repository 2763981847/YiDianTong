package com.oreki.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/18 10:45
 */
public interface WeixinService  {
    Map<String, Object> getQRCode(Long orderId);

    Map<String, String> getPayStatus(Long orderId);
}
