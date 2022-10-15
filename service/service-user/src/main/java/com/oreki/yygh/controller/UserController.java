package com.oreki.yygh.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.user.UserInfo;
import com.oreki.yygh.service.UserInfoService;
import com.oreki.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/13 19:03
 */
@Api(tags = "用户后台管理接口")
@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("条件查询用户分页")
    @GetMapping("/{page}/{limit}")
    public Result listUserPages(@PathVariable int page, @PathVariable int limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> userPages = userInfoService.listUserPages(page, limit, userInfoQueryVo);
        return Result.ok(userPages);
    }

    @ApiOperation("用户锁定或解锁")
    @PostMapping("/status/{id}/{status}")
    public Result updateStatus(@PathVariable long id, @PathVariable int status) {
        userInfoService.updateStatus(id, status);
        return Result.ok();
    }

    @ApiOperation("用户详情")
    @GetMapping("/details/{id}")
    public Result getUserDetails(@PathVariable long id) {
        Map<String, Object> userInfoDetails = userInfoService.getUserDetails(id);
        return Result.ok(userInfoDetails);
    }

    @ApiOperation("认证审批")
    @PostMapping("/approval/{id}/{authStatus}")
    public Result updateAuthStatus(@PathVariable long id, @PathVariable int authStatus) {
        userInfoService.updateAuthStatus(id, authStatus);
        return Result.ok();
    }
}
