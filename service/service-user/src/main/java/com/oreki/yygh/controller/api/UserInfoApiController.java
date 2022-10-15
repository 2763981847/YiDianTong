package com.oreki.yygh.controller.api;

import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.common.util.AuthContextHolder;
import com.oreki.yygh.model.user.UserInfo;
import com.oreki.yygh.service.UserInfoService;
import com.oreki.yygh.vo.user.LoginVo;
import com.oreki.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/9 17:10
 */
@Api(tags = "用户类接口")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("手机号登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String, Object> userInfo = userInfoService.login(loginVo);
        return userInfo == null ? Result.fail() : Result.ok(userInfo);
    }

    @ApiOperation("用户认证")
    @PostMapping("auth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        userInfoService.userAuth(userAuthVo, request);
        return Result.ok();
    }

    @ApiOperation("获取用户信息接口")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}

