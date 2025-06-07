package com.ducbrick.real_time_messaging_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RealTimeMessagingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealTimeMessagingApiApplication.class, args);
	}

}
