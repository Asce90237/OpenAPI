/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package common.dubbo;

import common.model.BaseResponse;
import common.model.entity.Auth;
import common.model.to.LeftNumUpdateTo;
import common.model.to.Oauth2ResTo;
import common.model.vo.LockChargingVo;

public interface ApiInnerService {

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 判断用户在该接口上是否还有调用次数
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean hasCount(long interfaceInfoId,long userId);

    /**
     * 根据ak查密钥信息
     * @param accessKey
     * @return
     */
    Auth getAuthByAk(String accessKey);

    /**
     * 判断接口是否有效
     * @param interfaceInfoId
     * @return
     */
    boolean apiIdIsValid(long interfaceInfoId);

    /**
     * 判断参数是否可以为空
     */
    boolean paramsIsValid(long interfaceInfoId);

    /**
     * 获取当前接口的剩余库存
     * @param interfaceInfoId
     * @return
     */
    String getPresentAvailablePieces(long interfaceInfoId);

    /**
     * 远程获取接口信息
     * @param interfaceInfoId
     * @return
     */
    BaseResponse getOrderInterfaceInfo(long interfaceInfoId);

    /**
     * 更新库存
     * @param lockChargingVo
     * @return
     */
    BaseResponse updateAvailablePieces(LockChargingVo lockChargingVo);

    /**
     * 远程解锁库存
     * @param lockChargingVo
     * @return
     */
    BaseResponse unlockAvailablePieces(LockChargingVo lockChargingVo);


    /**
     * 更新用户剩余可调用次数
     * @param leftNumUpdateTo
     * @return
     */
    BaseResponse updateUserLeftNum(LeftNumUpdateTo leftNumUpdateTo);

    /**
     * 第三方登录
     * @param oauth2ResTo
     * @param type
     * @return
     */
    BaseResponse oauth2Login(Oauth2ResTo oauth2ResTo, String type);

}
