package com.oreki.yygh.listener;

import com.oreki.common.rabbit.constant.MQConstant;
import com.oreki.yygh.service.SmsService;
import com.oreki.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/15 19:05
 */
@Component
public class SmsListener {
    @Resource
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstant.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MQConstant.EXCHANGE_DIRECT_MSM),
            key = {MQConstant.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        smsService.mqSend(msmVo);
    }

}
