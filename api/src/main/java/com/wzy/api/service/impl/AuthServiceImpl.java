package com.wzy.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.Exception.BusinessException;
import com.wzy.api.mapper.AuthMapper;
import com.wzy.api.model.entity.Auth;
import com.wzy.api.model.vo.AuthVo;
import com.wzy.api.service.AuthService;
import common.model.BaseResponse;
import common.model.enums.ErrorCode;
import common.Utils.ResultUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
* @author Asce
* @description 针对表【auth】的数据库操作Service实现
* @createDate 2023-02-01 15:24:16
*/
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth>
    implements AuthService {

    /**
     * 根据用户id获取用户的密钥
     * @param id
     * @param request
     * @return
     */
    @Override
    public BaseResponse<AuthVo> getAuthByUserId(Long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Auth auth = this.getOne(new QueryWrapper<Auth>().eq("userid", id));
        if(null == auth){
            throw new BusinessException(ErrorCode.AK_NOT_FOUND);
        }
        AuthVo authVo = new AuthVo();
        BeanUtils.copyProperties(auth,authVo);
        return ResultUtils.success(authVo);
    }

    /**
     * 修改API密钥的状态
     * @param id
     * @param request
     * @return
     */
    @Override
    public BaseResponse updateAuthStatus(Long id, HttpServletRequest request) {
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean userid = this.update(new UpdateWrapper<Auth>().eq("userid", id).setSql("status = ! status"));
        return userid == true ? ResultUtils.success("操作成功") : ResultUtils.error(ErrorCode.OPERATION_ERROR);
    }
}




