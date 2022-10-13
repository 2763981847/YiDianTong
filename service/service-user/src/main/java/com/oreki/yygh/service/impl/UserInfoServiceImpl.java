package com.oreki.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oreki.yygh.common.constant.UserConstant;
import com.oreki.yygh.common.exception.YyghException;
import com.oreki.yygh.common.helper.JwtHelper;
import com.oreki.yygh.common.result.ResultCodeEnum;
import com.oreki.yygh.common.util.AuthContextHolder;
import com.oreki.yygh.enums.AuthStatusEnum;
import com.oreki.yygh.model.user.UserInfo;
import com.oreki.yygh.service.UserInfoService;
import com.oreki.yygh.mapper.UserInfoMapper;
import com.oreki.yygh.vo.user.LoginVo;
import com.oreki.yygh.vo.user.UserAuthVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 27639
 * @description 针对表【user_info(用户表)】的数据库操作Service实现
 * @createDate 2022-10-09 17:06:25
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户手机号登录
     *
     * @param loginVo 登录信息
     * @return 用户名和token
     */
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //验证登录信息是否存在，不存在则抛出异常
        if (loginVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 拿到登录信息
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        // 检验手机号和验证码是否合法,不合法则抛出异常
        if (StringUtils.isEmpty(code) || !isValidChinesePhone(phone)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //  检验验证码是否一致
        String realCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(realCode)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //判断是否是绑定手机号码，不是则进行正常的手机号登录
        UserInfo userInfo = this.getByPhone(phone);
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            //手机号已注册过
            if (userInfo != null) {
                //将原来的数据删除
                LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserInfo::getOpenid, loginVo.getOpenid());
                boolean flag = this.remove(queryWrapper);
                if (flag) {
                    userInfo.setOpenid(loginVo.getOpenid());
                    this.updateById(userInfo);
                } else {
                    throw new YyghException(ResultCodeEnum.DATA_ERROR);
                }
            } else {
                userInfo = this.getByOpenid(loginVo.getOpenid());
                if (null != userInfo) {
                    userInfo.setPhone(loginVo.getPhone());
                    this.updateById(userInfo);
                } else {
                    throw new YyghException(ResultCodeEnum.DATA_ERROR);
                }
            }
        } else {
            //第一次登录则新增
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                //默认为可用状态
                userInfo.setStatus(1);
                //默认用户姓名
                userInfo.setName("");
                this.save(userInfo);
            }
            //不是第一次登录则判断账号是否已被禁用
            else if (userInfo.getStatus() == 0) {
                //已被禁用则抛出异常
                throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
            }
        }
        //封装name和token返回
        Map<String, Object> result = new HashMap<>();
        String name;
        //有姓名则返回姓名，无姓名则返回昵称，无昵称则返回手机号
        if (!StringUtils.isEmpty(name = userInfo.getName())
                || !StringUtils.isEmpty(name = userInfo.getNickName())
                || !StringUtils.isEmpty(name = userInfo.getPhone())) {
            result.put("name", name);
        }
        //生成token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        result.put("token", token);
        return result;
    }

    @Override
    public UserInfo getByOpenid(String openid) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getOpenid, openid);
        return this.getOne(queryWrapper);
    }

    @Override
    public UserInfo getByPhone(String phone) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone, phone);
        return this.getOne(queryWrapper);
    }

    @Override
    public void userAuth(UserAuthVo userAuthVo, HttpServletRequest request) {
        //从请求中拿到用户id和用户名
        Long userId = AuthContextHolder.getUserId(request);
        String userName = AuthContextHolder.getUserName(request);
        //构建更新wrapper
        LambdaUpdateWrapper<UserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInfo::getId, userId)
                .set(UserInfo::getName, userAuthVo.getName())
                .set(UserInfo::getCertificatesNo, userAuthVo.getCertificatesNo())
                .set(UserInfo::getCertificatesType, userAuthVo.getCertificatesType())
                .set(UserInfo::getCertificatesUrl, userAuthVo.getCertificatesUrl())
                .set(UserInfo::getAuthStatus, AuthStatusEnum.AUTH_RUN.getStatus());
        //操作数据库进行更新
        this.update(updateWrapper);
    }


    /**
     * 校验手机号是否合法
     *
     * @param phone 手机号
     * @return 是否合法
     */
    public static boolean isValidChinesePhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() != 11) {
            return false;
        }
        return UserConstant.CHINESE_PHONE_PATTERN.matcher(phone).matches();
    }
}




