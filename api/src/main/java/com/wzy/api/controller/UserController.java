package com.wzy.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzy.api.model.dto.request.DeleteRequest;
import com.wzy.api.model.dto.user.*;
import com.wzy.api.model.entity.User;
import com.wzy.api.model.vo.UserVO;
import com.wzy.api.service.UserService;
import common.model.BaseResponse;
import common.model.enums.ErrorCode;
import common.Exception.BusinessException;
import common.Utils.ResultUtils;
import common.model.to.Oauth2ResTo;
import common.model.vo.LoginUserVo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 用户接口
 *
 * 
 */
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("获取echarts需要展示的数据")
    @GetMapping("/getEchartsData")
    public BaseResponse getEchartsData(){
        return userService.getEchartsData();
    }

    @ApiOperation("获取全站用户数")
    @GetMapping("/getActiveUser")
    public BaseResponse getActiveUser(){
        return ResultUtils.success(userService.count(new QueryWrapper<User>().eq("isDelete",0)));
    }

    @ApiOperation("绑定用户手机号")
    @PostMapping("/bindPhone")
    public BaseResponse bindPhone(UserBindPhoneRequest userBindPhoneRequest,HttpServletRequest request){
        return userService.bindPhone(userBindPhoneRequest,request);
    }

    @ApiOperation("验证用户的登录状态")
    @PostMapping("/checkUserLogin")
    public BaseResponse checkUserLogin(HttpServletRequest request,HttpServletResponse response){
        return userService.checkUserLogin(request,response);
    }

    @ApiOperation("修改用户密码")
    @PostMapping("/updateUserPass")
    public BaseResponse updateUserPass(String password,HttpServletRequest request){
        return userService.updateUserPass(password,request);
    }

    @ApiOperation("忘记密码部分-验证手机号和验证码输入是否正确")
    @PostMapping("/authPassUserCode")
    public BaseResponse authPassUserCode(String code,HttpServletRequest request){
        return userService.authPassUserCode(code,request);
    }

    @ApiOperation("忘记密码请求第二步，发送验证码")
    @PostMapping("/sendPassUserCode")
    public BaseResponse sendPassUserCode(HttpServletRequest request) throws NoSuchAlgorithmException {
        return userService.sendPassUserCode(request);
    }


    @ApiOperation("用户忘记密码，返回用户注册时的手机号")
    @PostMapping("/getpassusertype")
    public BaseResponse getPassUserType(String username,HttpServletResponse response){
        return userService.getPassUserType(username,response);
    }

    @ApiOperation("生成图形验证码")
    @GetMapping("/getCaptcha")
    public void getCaptcha(HttpServletRequest request,HttpServletResponse response){
        userService.getCaptcha(request,response);
    }

    @ApiOperation("用户通过手机号进行登录")
    @PostMapping("/loginBySms")
    public BaseResponse loginBySms(@RequestBody UserLoginBySmsRequest loginBySms , HttpServletResponse response){
        return userService.loginBySms(loginBySms,response);
    }


    @ApiOperation("向手机号发送短信")
    @GetMapping("/captcha")
    public BaseResponse captcha(@RequestParam String mobile){
        return userService.captcha(mobile);
    }


    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest ,HttpServletRequest request) {
        long result = userService.userRegister(userRegisterRequest,request);
        return ResultUtils.success(result);
    }

    @ApiOperation("用户通过用户名和密码登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLoginByPwd(UserLoginRequest userLoginRequest, HttpServletResponse response) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVo user = userService.userLogin(userAccount, userPassword, response);
        return ResultUtils.success(user);
    }


    @GetMapping("/get/login")
    @ApiOperation("获取当前登录用户")
    public BaseResponse<UserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }


    @ApiOperation("用户注销")
    @GetMapping("/logout")
    public BaseResponse logoutSuccess(HttpServletResponse response){
        return userService.logout(response);
    }


    @ApiOperation("删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    @ApiOperation("更新用户")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // todo 用户名和手机号不能重复
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }


    @ApiOperation("根据 id 获取用户")
    @GetMapping("/get")
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    @ApiOperation("获取用户列表")
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        return userService.lisUser(userQueryRequest);
    }

    @ApiOperation("分页获取用户列表")
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        return userService.listUserByPage(userQueryRequest);
    }
}
