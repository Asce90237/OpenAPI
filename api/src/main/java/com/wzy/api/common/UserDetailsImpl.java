package com.wzy.api.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wzy.api.model.entity.LoginUser;
import com.wzy.api.model.entity.User;
import com.wzy.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Asce
 */
@Service
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询用户信息
        if (username == null || username.length() == 0) {
            throw new RuntimeException("用户名不能为空");
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUserAccount, username).or().eq(User::getMobile, username);
        User user = userService.getOne(lambdaQueryWrapper);
        if (user == null){
            throw new RuntimeException("用户名不存在");
        }
        //隐藏手机号
        String mobile = user.getMobile();
        String newMobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
        user.setMobile(newMobile);
        //封装权限
        List<String> permissions = new ArrayList<>();
        permissions.add("ROLE_" + user.getUserRole());
        LoginUser loginUser = new LoginUser(user, permissions);
        return loginUser;
    }
}
