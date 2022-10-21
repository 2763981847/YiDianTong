package com.oreki.yygh.order.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/18 10:20
 */
@Component
public class WXConstantProperties implements InitializingBean {

    @Value("${weixin.pay.appid}")
    private String appid;

    @Value("${weixin.pay.partner}")
    private String partner;

    @Value("${weixin.pay.partnerKey}")
    private String partnerKey;

    @Value("${weixin.pay.certPath}")
    private String certPath;

    public static String APPID;
    public static String PARTNER;
    public static String PARTNER_KEY;

    public static String CERT_PATH;

    @Override
    public void afterPropertiesSet() throws Exception {
        APPID = appid;
        PARTNER = partner;
        PARTNER_KEY = partnerKey;
        CERT_PATH = certPath;
    }
}
