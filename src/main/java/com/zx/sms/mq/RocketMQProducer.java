package com.zx.sms.mq;

import com.alibaba.fastjson.JSON;
import com.zx.sms.entity.CmppReportMessage;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RocketMQProducer {

    private static final Logger logger= LoggerFactory.getLogger(RocketMQProducer.class);

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendReportMessage(CmppReportMessage cmppReportMessage){
        String reportMsg=JSON.toJSONString(cmppReportMessage);
        Message<String> message=   MessageBuilder.withPayload(reportMsg).build();
        rocketMQTemplate.asyncSend(MqConstant.TOPIC, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {

            }

            @Override
            public void onException(Throwable e) {
                logger.error("report msg send fail,msg:{}",reportMsg,e);
            }
        },3000,2);
    }

}
