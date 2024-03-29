package com.oreki.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.enums.PaymentTypeEnum;
import com.oreki.yygh.enums.RefundStatusEnum;
import com.oreki.yygh.model.order.OrderInfo;
import com.oreki.yygh.model.order.PaymentInfo;
import com.oreki.yygh.model.order.RefundInfo;
import com.oreki.yygh.order.service.OrderInfoService;
import com.oreki.yygh.order.service.RefundInfoService;
import com.oreki.yygh.order.service.WeixinService;
import com.oreki.yygh.order.util.HttpClient;
import com.oreki.yygh.order.util.WXConstantProperties;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/18 10:46
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private PaymentInfoServiceImpl paymentInfoService;

    @Resource
    private RefundInfoService refundInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> getQRCode(Long orderId) {
        //若Redis中有该订单数据，直接返回
        Map<String, Object> result = (Map<String, Object>) redisTemplate.opsForValue().get(orderId.toString());
        if (result != null) {
            return result;
        }
        //根据订单id拿到订单信息
        OrderInfo orderInfo = orderInfoService.getOrderDetails(orderId);
        //向支付记录表中添加信息
        paymentInfoService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        //1、设置参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", WXConstantProperties.APPID);
        paramMap.put("mch_id", WXConstantProperties.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        String body = orderInfo.getReserveDate() + "就诊" + orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1");
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");
        //HTTPClient来根据URL访问第三方接口并且传递参数
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        //client设置参数
        try {
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, WXConstantProperties.PARTNER_KEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            //将结果放入Redis中
            if (!StringUtils.isEmpty(resultMap.get("result_code"))) {
                //设置一小时过期时间
                redisTemplate.opsForValue().set(orderId.toString(), map, 1, TimeUnit.HOURS);
            }
            return map;
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.SERVICE_ERROR);
        }
    }

    /**
     * 调用微信接口查询订单支付状态
     *
     * @param orderId 要查询的订单id
     * @return 支付状态
     */
    @Override
    public Map<String, String> getPayStatus(Long orderId) {
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        //1、封装参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", WXConstantProperties.APPID);
        paramMap.put("mch_id", WXConstantProperties.PARTNER);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, WXConstantProperties.PARTNER_KEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 微信支付退款
     *
     * @param orderId 订单id
     * @return 退款是否成功
     */
    @Override
    public boolean refund(Long orderId) {
        //获取到支付记录
        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //新增到退款记录表中
        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
        //判断当前订单是否已经退款
        if (RefundStatusEnum.REFUND.getStatus().equals(refundInfo.getRefundStatus())) {
            return true;
        }
        //调用微信接口退款
        Map<String, String> paramMap = new HashMap<>(8);
        paramMap.put("appid", WXConstantProperties.APPID);       //公众账号ID
        paramMap.put("mch_id", WXConstantProperties.PARTNER);   //商户编号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1");
        paramMap.put("refund_fee", "1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, WXConstantProperties.PARTNER_KEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(WXConstantProperties.PARTNER);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (!resultMap.isEmpty() && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
