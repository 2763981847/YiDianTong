package com.oreki.yygh.order.controller.api;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.enums.PaymentTypeEnum;
import com.oreki.yygh.order.service.OrderInfoService;
import com.oreki.yygh.order.service.PaymentInfoService;
import com.oreki.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/18 10:43
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {
    @Resource
    private WeixinService weixinService;

    @Resource
    private PaymentInfoService paymentInfoService;

    @ApiOperation("生成微信支付二维码")
    @GetMapping("QRCode/{orderId}")
    public Result getQRCode(@PathVariable Long orderId) {
        Map<String, Object> map = weixinService.getQRCode(orderId);
        return Result.ok(map);
    }

    @ApiOperation("根据订单号到微信查询支付状态")
    @GetMapping("payStatus/{orderId}")
    public Result getPayStatus(@PathVariable Long orderId) {
        //调用微信接口查询支付状态
        Map<String, String> resultMap = weixinService.getPayStatus(orderId);
        //获取失败
        if (resultMap == null) {
            return Result.fail().message("支付出错");
        }
        //如果支付成功成功
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更改订单状态，处理支付结果
            String outTradeNo = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(outTradeNo, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");

    }


}
