package com.zx.sms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.zx.sms.config.SmsServerConfiguration;
import com.zx.sms.connect.manager.EndpointManager;

import javax.annotation.Resource;

@Component
public class ServerStartListener implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(ServerStartListener.class);

	@Resource
	private SmsServerConfiguration config ;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			config.initLoad();
			final EndpointManager manager = EndpointManager.INS;
			List serverlist = config.loadServerEndpointEntity();// 服务终端实体类集合
			manager.addAllEndpointEntity(serverlist);
			
			logger.info("load server complete.");
			try {
				manager.openAll();
			} catch (Exception e) {
				logger.error("load Server error.",e);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
