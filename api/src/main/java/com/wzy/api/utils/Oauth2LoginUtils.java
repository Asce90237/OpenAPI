package com.wzy.api.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.mapper.UserMapper;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.entity.LoginUser;
import com.wzy.api.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asce
 */
@Component
public class Oauth2LoginUtils {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private GenerateKeyUtil generateKeyUtil;

    @Resource
    private AuthMapper authMapper;

    /**
     * 通过gitee 或者 github 进行登录
     * @param response
     * @return
     */
    public LoginUser giteeOrGithubOauth2Login(String type, HttpResponse response){
        JSONObject obj = JSONUtil.parseObj(response.body());
        String userAccount = type + obj.get("id");
        String name = String.valueOf(obj.get("login"));
        String userAvatar = String.valueOf(obj.get("avatar_url"));
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        if (user == null) {
            String appId = String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, 9 - 1)));
            String accessKey = generateKeyUtil.generateAk(userAccount);
            String secretKey = generateKeyUtil.generateSk(userAccount);
            Auth auth = new Auth();
            auth.setUseraccount(userAccount);
            auth.setAppid(Integer.valueOf(appId));
            auth.setAccesskey(accessKey);
            auth.setSecretkey(secretKey);
            User user1 = new User();
            user1.setUserAccount(userAccount);
            user1.setUserName(name);
            user1.setUserAvatar(userAvatar);
            userMapper.insert(user1);
            auth.setUserid(user1.getId());
            authMapper.insert(auth);
            // 进行登录操作
            user = userMapper.selectById(user1.getId());
        }
        List<String> permissions = new ArrayList<>();
        permissions.add("ROLE_" + user.getUserRole());
        LoginUser loginUser = new LoginUser(user, permissions);
        return loginUser;
    }
}
