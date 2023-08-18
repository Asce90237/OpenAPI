package com.wzy.order.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import com.wzy.order.feign.UserFeignServices;
import com.wzy.order.model.entity.ApiOrder;
import com.wzy.order.model.entity.ApiOrderLock;
import com.wzy.order.service.ApiOrderLockService;
import com.wzy.order.service.ApiOrderService;
import common.model.BaseResponse;
import common.model.vo.LockChargingVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Asce
 */
@Slf4j
@Component
public class OrderUnLockeListener {

    @Autowired
    private ApiOrderLockService apiOrderLockService;

    @Autowired
    private ApiOrderService apiOrderService;

    @Autowired
    private UserFeignServices userFeignServices;

    /**
     * 监听死信队列 - 记录订单超时未支付的消息后的日志
     * @param apiOrder
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(queues = "order.delay.queue")
    public void delayListener(ApiOrder apiOrder, Message message, Channel channel) throws IOException {
        try{
            log.error("监听到死信队列消息==>{}",apiOrder);
            Long id = apiOrder.getId();
            ApiOrder byId = apiOrderService.getById(id);
            if (null == byId){
                //库存已经扣了，但是出现异常，未能够生成订单和锁定订单结果
                Long orderNum = apiOrder.getOrderNum();
                LockChargingVo lockChargingVo = new LockChargingVo();
                lockChargingVo.setOrderNum(orderNum);
                lockChargingVo.setInterfaceid(apiOrder.getInterfaceId());
                BaseResponse baseResponse = userFeignServices.unlockAvailablePieces(lockChargingVo);
                if (baseResponse.getCode() != 0) {
                    throw new RuntimeException();
                }
                //false不批量确认，只确认当前消息被消费
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }
            Integer status = byId.getStatus();
            //api_order:0-待付款，1-已完成，2-无效订单
            //order_lock:0-已解锁，1-已锁定，2-已扣减
            if (status == 0) {
                Long orderNum = apiOrder.getOrderNum();
                //订单未完成
                String orderSn = byId.getOrderSn();
                //更新库存状态信息表
                apiOrderLockService.update(new UpdateWrapper<ApiOrderLock>().eq("orderSn", orderSn).set("lockStatus",0));
                //更新订单表状态
                apiOrderService.update(new UpdateWrapper<ApiOrder>().eq("id",apiOrder.getId()).set("status",2));
                //远程调用 - 解锁库存
                LockChargingVo lockChargingVo = new LockChargingVo();
                lockChargingVo.setOrderNum(orderNum);
                lockChargingVo.setInterfaceid(apiOrder.getInterfaceId());
                BaseResponse baseResponse = userFeignServices.unlockAvailablePieces(lockChargingVo);
                if (baseResponse.getCode() != 0) {
                    throw new RuntimeException();
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
