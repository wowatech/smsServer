package com.zx.sms;

import java.util.concurrent.locks.LockSupport;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Configurable
@SpringBootApplication
public class MockServerApplication {
	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(MockServerApplication.class, args);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		LockSupport.park();
	}
}
