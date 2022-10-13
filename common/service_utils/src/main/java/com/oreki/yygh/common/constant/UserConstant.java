package com.oreki.yygh.common.constant;

import java.util.regex.Pattern;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/9 20:29
 */
public class UserConstant {
    //中国手机号正则匹配式
    public static final Pattern CHINESE_PHONE_PATTERN = Pattern.compile("((13|15|17|18)\\d{9})|(14[57]\\d{8})");
    //token过期时间（1天）
    public static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000;
    //token签名密钥
    public static final String TOKEN_SIGN_KEY = "oreki";
}
