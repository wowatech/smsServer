package com.zx.sms.mq;

import com.alibaba.fastjson.JSONObject;
import com.zx.sms.codec.cmpp.msg.CmppDeliverRequestMessage;
import com.zx.sms.common.util.ChannelUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;
import com.zx.sms.converter.CmppReportMessageConverter;
import com.zx.sms.entity.CmppReportMessage;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@RocketMQMessageListener(topic = MqConstant.TOPIC,consumerGroup = MqConstant.GROUP)
public class RocketMQConsumer implements RocketMQListener<String> {

    private static final Logger logger= LoggerFactory.getLogger(RocketMQConsumer.class);

    @Resource
    private RocketMQProducer rocketMQProducer;

    @Override
    public void onMessage(String s) {
        if (StringUtils.isBlank(s)){
            return;
        }

        try{
            CmppReportMessage message= JSONObject.parseObject(s,CmppReportMessage.class);
            if (message==null){
                return;
            }

            final EndpointManager manager = EndpointManager.INS;
            EndpointEntity entity= manager.getEndpointEntity(message.getId());
            if (entity==null){
                return;
            }
            deliverReport(entity,CmppReportMessageConverter.convert(message), message);
        }catch (Exception e){
            logger.error("report msg consume fail,msg:{}",s,e);
        }

    }

    private void deliverReport(EndpointEntity entity,CmppDeliverRequestMessage message,CmppReportMessage reportMessage) {
        ChannelUtil.asyncWriteToEntity(entity,message, new GenericFutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                if (!future.isSuccess()) {
                    logger.error("report msg send fail,msg:{}",reportMessage);
                    rocketMQProducer.sendReportMessage(reportMessage);
                }
            }
        });
    }
}
