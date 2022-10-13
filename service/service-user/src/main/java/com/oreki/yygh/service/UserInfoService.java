package com.oreki.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oreki.yygh.model.user.UserInfo;
import com.oreki.yygh.vo.user.LoginVo;
import com.oreki.yygh.vo.user.UserAuthVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 27639
 * @description 针对表【user_info(用户表)】的数据库操作Service
 * @createDate 2022-10-09 17:06:25
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);

    UserInfo getByOpenid(String openid);

    UserInfo getByPhone(String phone);

    void userAuth(UserAuthVo userAuthVo, HttpServletRequest request);
}
