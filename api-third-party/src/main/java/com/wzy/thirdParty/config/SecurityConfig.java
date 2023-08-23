package com.wzy.thirdParty.config;

import com.wzy.thirdParty.filter.JWTAuthenticationTokenFilter;
import com.wzy.thirdParty.utils.SimpleAccessDeniedHandler;
import com.wzy.thirdParty.utils.SimpleAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Asce
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SimpleAuthenticationEntryPoint simpleAuthenticationEntryPoint;

    @Autowired
    private SimpleAccessDeniedHandler simpleAccessDeniedHandler;

    @Autowired
    private JWTAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 配置PasswordEncoder
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置authenticationManagerBean
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 放行静态资源
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //所需要用到的静态资源，允许访问
        web.ignoring().antMatchers( "/swagger-ui.html",
                "/swagger-ui/*",
                "/swagger-resources/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/webjars/**",
                "/doc.html");
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //允许跨域
//                .cors().and()
                //关闭csrf
                .csrf().disable()
                //不通过session获取security context
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/**", "/alipay/notify","/alipay/queryTradeStatus").permitAll()
                // 除上面外的所有请求全部需要鉴权认证,.authenticated()表示认证之后可以访问
                .anyRequest().authenticated();
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        //注册自定义异常响应
        http.exceptionHandling()
                .accessDeniedHandler(simpleAccessDeniedHandler) //访问拒绝，权限不足
                .authenticationEntryPoint(simpleAuthenticationEntryPoint); //身份未验证
    }
}
