package com.oreki.yygh.common.util;

import com.oreki.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取当前用户信息的工具类
 *
 * @author : Fu QiuJie
 * @date : 2022/10/13 8:35
 */
public class AuthContextHolder {
    /**
     * 拿到当前用户id
     * @param request 请求
     * @return 用户ID
     */
    public static Long getUserId(HttpServletRequest request){
        //从请求头中拿到token
        String token = request.getHeader("token");
        //从token中拿到用户ID
        return JwtHelper.getUserId(token);
    }

    /**
     * 拿到当前用户名
     * @param request 请求
     * @return 用户名
     */
    public static String getUserName(HttpServletRequest request){
        //从请求头中拿到token
        String token = request.getHeader("token");
        //从token中拿到用户名
        return JwtHelper.getUserName(token);
    }
}
