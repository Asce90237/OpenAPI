package com.wzy.api.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzy.api.model.entity.User;
import com.wzy.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
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
        if (username == null || username.length() == 0) {
            throw new RuntimeException("用户名不能为空");
        }
        User user = userService.getOne(new QueryWrapper<User>().eq("userAccount", username).or().eq("mobile", username));
        if (user == null){
            throw new RuntimeException("用户不存在");
        }
//        List<SimpleGrantedAuthority> authorities=new ArrayList<>();
        //根据userId获取Role对象
//        String[] roles = user.getUserRole().split(",");
//        for (String role : roles) {
//            authorities.add(new SimpleGrantedAuthority("ROLE_"+role));
//        }
//        Collection<? extends GrantedAuthority> userAuthorities = user.getAuthorities();
        //隐藏手机号
        String mobile = user.getMobile();
        String newMobile = mobile.substring(0, 3) + "****" + mobile.substring(7);
        user.setMobile(newMobile);
        return user;
    }
}
