package com.oreki.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.oreki.yygh.common.constant.UserConstant;
import com.oreki.yygh.service.SmsService;
import com.oreki.yygh.utils.ConstantPropertiesUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/10 19:13
 */
@Service
public class SmsServiceImpl implements SmsService {
    @Override
    public boolean sendCode(String phone, String code) {
        //手机号不正确则返回
        if (!UserConstant.CHINESE_PHONE_PATTERN.matcher(phone).matches()) {
            return false;
        }
        //整合阿里云短信服务
        //设置相关参数
        DefaultProfile profile = DefaultProfile.
                getProfile(ConstantPropertiesUtil.REGION_Id,
                        ConstantPropertiesUtil.ACCESS_KEY_ID,
                        ConstantPropertiesUtil.SECRET);
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        //手机号
        request.putQueryParameter("PhoneNumbers", phone);
        //签名名称
        request.putQueryParameter("SignName", "阿里云短信测试");
        //模板code
        request.putQueryParameter("TemplateCode", "SMS_154950909");
        //验证码  使用json格式   {"code":"123456"}
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));
        //调用方法进行短信发送
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;

    }
}
