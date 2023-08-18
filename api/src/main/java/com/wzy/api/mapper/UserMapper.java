package com.wzy.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzy.api.model.entity.User;
import common.model.vo.EchartsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Entity com.wzy.api.model.domain.User
 */
public interface UserMapper extends BaseMapper<User> {

    String getMobile(@Param("username") String username);

    String getUserNameByPhone(@Param("username") String username);

    /**
     * 查询手机号是否存在
     * @param mobile
     * @return
     */
    boolean phoneExits(@Param("mobile") String mobile);

    List<EchartsVo> getUserList(@Param("dateList") List<String> dateList);

    /**
     * 查询账户是否存在
     * @param userAccount
     * @return
     */
    boolean userAccountExits(String userAccount);
}




