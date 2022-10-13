package com.oreki.yygh.service;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/10 19:13
 */
public interface SmsService {
    boolean sendCode(String phone, String code);
}
