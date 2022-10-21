package com.oreki.yygh.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.oreki.common.rabbit.constant.MQConstant;
import com.oreki.common.rabbit.service.RabbitService;
import com.oreki.yygh.model.hosp.Schedule;
import com.oreki.yygh.service.ScheduleService;
import com.oreki.yygh.vo.msm.MsmVo;
import com.oreki.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/15 19:16
 */
@Component
public class HospitalListener {
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private RabbitService rabbitService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstant.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MQConstant.EXCHANGE_DIRECT_ORDER),
            key = {MQConstant.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
        if (null != orderMqVo.getAvailableNumber()) {
            //下单成功更新预约数
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        } else {
            //取消预约更新预约数
            schedule.setAvailableNumber(schedule.getAvailableNumber() + 1);
        }
        scheduleService.mqUpdate(schedule);
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (null != msmVo) {
            rabbitService.sendMessage(MQConstant.EXCHANGE_DIRECT_MSM, MQConstant.ROUTING_MSM_ITEM, msmVo);
        }
    }

}

