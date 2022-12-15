package com.oreki.yygh.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 * @author 27639
 */
@Getter
public enum ResultCodeEnum {

    CANCEL_ORDER_FAIL(225, "取消订单失败"),
    CANCEL_ORDER_NO(225, "不能取消预约"),
    CODE_ERROR(210, "验证码错误"),
    DATA_ERROR(204, "数据异常"),
    DATA_UPDATE_ERROR(205, "数据版本异常"),
    FAIL(201, "失败"),

    FETCH_ACCESSTOKEN_FAILD(218, "获取accessToken失败"),
    FETCH_USERINFO_ERROR(219, "获取用户信息失败"), //LOGIN_ERROR( 23005, "登录失败"),

    HOSCODE_EXIST(230, "医院编号已经存在"),
    HOSPITAL_LOCK(320, "医院被锁定，暂时不能访问"),
    HOSPITAL_OPEN(310, "医院未开通，暂时不能访问"),
    ILLEGAL_CALLBACK_REQUEST_ERROR(217, "非法回调请求"),
    LOGIN_ACL(215, "没有权限"),

    LOGIN_AURH(214, "需要登录"),
    LOGIN_AUTH(208, "未登陆"),
    LOGIN_DISABLED_ERROR(212, "该用户已被禁用") //    LOGIN_MOBLE_ERROR(211, "账号不正确"),
    ,
    NUMBER_NO(240, "可预约号不足"),


    PARAM_ERROR(202, "参数不正确"),
    PAY_RUN(220, "支付中"),
    PERMISSION(209, "没有权限"),

    REGISTER_MOBLE_ERROR(213, "手机号已被使用"),
    SERVICE_ERROR(203, "服务异常"),
    SIGN_ERROR(300, "签名错误"),

    SUCCESS(200, "成功"),
    TIME_NO(250, "当前时间不可以预约"),
    URL_ENCODE_ERROR(216, "URL编码失败"),
    ;

    private Integer code;
    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
