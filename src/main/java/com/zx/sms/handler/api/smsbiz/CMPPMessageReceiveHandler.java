package com.zx.sms.handler.api.smsbiz;

import com.zx.sms.codec.cmpp.msg.*;
import com.zx.sms.common.util.CachedMillisecondClock;
import com.zx.sms.config.SpringContextUtil;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.converter.CmppReportMessageConverter;
import com.zx.sms.entity.CmppReportMessage;
import com.zx.sms.mq.RocketMQProducer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


@Component
public class CMPPMessageReceiveHandler extends MessageReceiveHandler {

	private static final Logger logger= LoggerFactory.getLogger(CMPPMessageReceiveHandler.class);



	@Override
	protected ChannelFuture reponse(final ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof CmppDeliverRequestMessage) {
			CmppDeliverRequestMessage e = (CmppDeliverRequestMessage) msg;
			
			if(e.getFragments()!=null) {
				//长短信会带有片断
				for(CmppDeliverRequestMessage frag:e.getFragments()) {
					CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(frag.getHeader().getSequenceId());
					responseMessage.setResult(0);
					responseMessage.setMsgId(frag.getMsgId());
					ctx.channel().write(responseMessage);
				}
			}
			
			CmppDeliverResponseMessage responseMessage = new CmppDeliverResponseMessage(e.getHeader().getSequenceId());
			responseMessage.setResult(0);
			responseMessage.setMsgId(e.getMsgId());
			return ctx.channel().writeAndFlush(responseMessage);

		}else if (msg instanceof CmppSubmitRequestMessage) {
			//接收到 CmppSubmitRequestMessage 消息
			CmppSubmitRequestMessage e = (CmppSubmitRequestMessage) msg;
			
			final List<CmppDeliverRequestMessage> reportlist = new ArrayList<CmppDeliverRequestMessage>();
			
			if(e.getFragments()!=null) {
				//长短信会可能带有片断，每个片断都要回复一个response
				for(CmppSubmitRequestMessage frag:e.getFragments()) {
					CmppSubmitResponseMessage responseMessage = new CmppSubmitResponseMessage(frag.getHeader().getSequenceId());
					responseMessage.setResult(0);
					ctx.channel().write(responseMessage);
					
					CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
					deliver.setDestId(e.getSrcId());
					deliver.setSrcterminalId(e.getDestterminalId()[0]);
					CmppReportRequestMessage report = new CmppReportRequestMessage();
					report.setDestterminalId(deliver.getSrcterminalId());
					report.setMsgId(responseMessage.getMsgId());
					String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
					report.setSubmitTime(t);
					report.setDoneTime(t);
					report.setStat("DELIVRD");
					report.setSmscSequence(0);
					deliver.setReportRequestMessage(report);
					reportlist.add(deliver);
				}
			}


			
			final CmppSubmitResponseMessage resp = new CmppSubmitResponseMessage(e.getHeader().getSequenceId());
			resp.setResult(0);
			
			ChannelFuture future = ctx.channel().writeAndFlush(resp);
			
			//回复状态报告
			if(e.getRegisteredDelivery()==1) {
				
				final CmppDeliverRequestMessage deliver = new CmppDeliverRequestMessage();
				deliver.setDestId(e.getSrcId());
				deliver.setSrcterminalId(e.getDestterminalId()[0]);
				CmppReportRequestMessage report = new CmppReportRequestMessage();
				report.setDestterminalId(deliver.getSrcterminalId());
				report.setMsgId(resp.getMsgId());
				String t = DateFormatUtils.format(CachedMillisecondClock.INS.now(), "yyMMddHHmm");
				report.setSubmitTime(t);
				report.setDoneTime(t);
				report.setStat("DELIVRD");
				report.setSmscSequence(0);
				deliver.setReportRequestMessage(report);
				reportlist.add(deliver);

				Executor executor=	SpringContextUtil.getBean("mqExecutor",Executor.class);


				EndpointEntity entity= getEndpointEntity();



				executor.execute(()->{
					try{
						for (CmppDeliverRequestMessage cmppDeliverRequestMessage:reportlist){

							CmppReportMessage cmppReportMessage= CmppReportMessageConverter.reConvert(cmppDeliverRequestMessage,entity);
							RocketMQProducer rocketMQProducer=	SpringContextUtil.getBean(RocketMQProducer.class);
							if (rocketMQProducer!=null){
								rocketMQProducer.sendReportMessage(cmppReportMessage);
							}

						}
					}catch (Exception e1){
						logger.error("生产状态报告失败",e);
					}
				});
			}
			return  future ;
		}
		return null;
	}

}
