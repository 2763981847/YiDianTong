package com.oreki.yygh.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oreki.yygh.common.helper.JwtHelper;
import com.oreki.yygh.common.result.Result;
import com.oreki.yygh.model.user.UserInfo;
import com.oreki.yygh.service.UserInfoService;
import com.oreki.yygh.utils.ConstantWxPropertiesUtil;
import com.oreki.yygh.utils.HttpClientUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Fu QiuJie
 * @date : 2022/10/11 11:15
 */
@Api(tags = "微信登录接口")
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeiXinApiController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("返回生成二维码所需要的参数")
    @GetMapping("/getQRParam")
    @ResponseBody
    public Result getQRParam() throws UnsupportedEncodingException {
        Map<String, Object> param = new HashMap<>();
        param.put("appid", ConstantWxPropertiesUtil.WX_OPEN_APP_ID);
        param.put("scope", "snsapi_login");
        param.put("state", String.valueOf(System.currentTimeMillis()));//System.currentTimeMillis()+""
        //重定向地址需要先进行解码
        String redirectUrl = ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL;
        String encode = URLEncoder.encode(redirectUrl, "utf-8");
        param.put("redirectUri", encode);
        return Result.ok(param);
    }

    @ApiOperation("扫描二维码之后的回调函数")
    @GetMapping("/callback")
    public String callback(String code, String state) throws UnsupportedEncodingException {
        //使用code和appid以及appscrect换取access_token
        //构建请求地址
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        String stringAccessToken;
        try {
            // 使用httpclient请求这个地址
            stringAccessToken = HttpClientUtil.get(accessTokenUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //从字符串中获取到access_token和openid
        JSONObject jsonObject = JSON.parseObject(stringAccessToken);
        String accessToken = jsonObject.getString("access_token");
        String openId = jsonObject.getString("openid");
        //根据openId查找数据库中是否已存在该用户
        UserInfo info = userInfoService.getByOpenid(openId);
        if (info == null) {
         //用access_token和openid请求微信地址，拿到登录人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String userInfo;
            try {
                userInfo = HttpClientUtil.get(userInfoUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JSONObject parseObject = JSON.parseObject(userInfo);
            //解析用户信息
            String nickname = parseObject.getString("nickname");
            //封装用户信息
            info = new UserInfo();
            info.setNickName(nickname);
            info.setOpenid(openId);
            info.setStatus(1);
            //保存用户信息到数据库
            userInfoService.save(info);
            //更新info，主要是为了更新获取到保存后的id
            info = userInfoService.getByOpenid(openId);
        }
        //封装返回结果集
        String name = info.getName();
        if (StringUtils.isEmpty(name)) {
            name = info.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = info.getPhone();
        }
        String openid = StringUtils.isEmpty(info.getPhone()) ? info.getOpenid() : "";
        String token = JwtHelper.createToken(info.getId(), name);
        //跳转到前端页面
        return "redirect:" + ConstantWxPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token=" + token + "&openid=" + openid + "&name=" + URLEncoder.encode(name, "utf-8");


    }

}
