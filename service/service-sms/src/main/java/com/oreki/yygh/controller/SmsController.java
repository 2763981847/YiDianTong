package com.oreki.yygh.controller;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.service.SmsService;
import com.oreki.yygh.utils.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/10 19:11
 */
@Api(tags = "短信接口")
@RestController
@RequestMapping("/api/sms")
public class SmsController {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private SmsService smsService;

    @ApiOperation("发送短信验证码")
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        //如果Redis中已经存在验证码则直接返回
        if (redisTemplate.opsForValue().get(phone) != null) {
            return Result.ok();
        }
        //通过工具类生成一个六位验证码
        String code = RandomUtil.getSixBitRandom();
        //调用短信发送服务
        boolean isSend = smsService.sendCode(phone, code);
        if (isSend) {
            //发送成功则将验证码存入Redis，并设置5分钟有效时间
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return Result.ok();
        } else return Result.fail().message("短信发送失败");
    }
}
