package com.wzy.api.utils;

import common.constant.RabbitMqConstant;
import common.model.to.SmsTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 向mq发送消息
 * 以及消息的可靠性实现
 */
@Slf4j
@Component                                         //确认回调接口，生产者发消息无论到不到交换机都会触发
public class RabbitUtils implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;


    private String finalId = null;

    private SmsTo smsTempTo = null;

    /**
     * 向mq中投递发送短信消息
     * @param smsTo
     * @throws Exception
     */
    public void sendSms(SmsTo smsTo) {
        String messageId = null;
        try {
            // 将 headers 添加到 MessageProperties 中，并发送消息
            messageId = UUID.randomUUID().toString();
            HashMap<String , Object> hashMap = new HashMap<>();
            hashMap.put("retryCount",0);
            hashMap.put("status",0); //消息状态：0-未投递、1-已投递
            hashMap.put("smsTo",smsTo);
            //将重试次数和短信发送状态存入redis中去
            redisTemplate.opsForHash().putAll(RabbitMqConstant.SMS_HASH_PREFIX+messageId,hashMap);
            redisTemplate.expire(RabbitMqConstant.SMS_HASH_PREFIX+messageId,10, TimeUnit.MINUTES);
            String finalMessageId = messageId;
            finalId = messageId;
            smsTempTo = smsTo;
            rabbitTemplate.convertAndSend(RabbitMqConstant.sms_exchange,RabbitMqConstant.sms_routingKey,smsTo, message -> {
                //在发送消息之前对消息进行定制化处理,可以修改它的属性,message即为要发送的消息本身
                MessageProperties messageProperties = message.getMessageProperties();
                //生成全局唯一id
                messageProperties.setMessageId(finalMessageId);
                messageProperties.setContentEncoding("utf-8");
                return message;
            });
        }catch (Exception e){
            //出现异常，删除该短信id对应的redis，并将该失败消息存入到“死信”redis中去，然后使用定时任务去扫描该key，并重新发送到mq中去
            redisTemplate.delete(RabbitMqConstant.SMS_HASH_PREFIX+messageId);
            redisTemplate.opsForHash().put(RabbitMqConstant.MQ_PRODUCER,messageId,smsTo);
            throw new RuntimeException(e);
        }
    }

    /**
     * 1、只要消息抵达交换机，那么b=true
     * @param correlationData 当前消息的唯一关联数据（消息的唯一id）
     * @param b 消息是否被交换机收到
     * @param s 失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if (b){
            //投递成功则修改Redis中的状态
            redisTemplate.opsForHash().put(RabbitMqConstant.SMS_HASH_PREFIX+finalId,"status",1);
        }else {
            //确认消息是否投递成功，消息投递失败时，将该消息存入失败“死信”Redis中去，并从原来的Redis中删除
            log.error("消息投递到交换机失败：{}---->{}",correlationData,s);
            redisTemplate.delete(RabbitMqConstant.SMS_HASH_PREFIX+finalId);
            redisTemplate.opsForHash().put(RabbitMqConstant.MQ_PRODUCER, finalId,smsTempTo);
        }
    }



    /**
     * 设置消息抵达队列的确认
     * 有消息投递到指定的队列失败后，才会触发该回调
     * @param returnedMessage
     */
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.error("消息投递队列失败---{}",returnedMessage);
        redisTemplate.delete(RabbitMqConstant.SMS_HASH_PREFIX+finalId);
        redisTemplate.opsForHash().put(RabbitMqConstant.MQ_PRODUCER,finalId,smsTempTo);
    }


    //注入
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
}
