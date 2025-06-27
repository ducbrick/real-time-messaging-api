package com.ducbrick.real_time_messaging_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
	@Bean
	public AuthorizationManager<Message<?>> msgAuthMan() {
		var msg = MessageMatcherDelegatingAuthorizationManager.builder();

		msg
				.simpMessageDestMatchers("/app/private-msg").authenticated()
				.simpSubscribeDestMatchers("/user/queue/private-msg").authenticated()
				.simpTypeMatchers(SimpMessageType.SUBSCRIBE).denyAll()
				.simpTypeMatchers(SimpMessageType.MESSAGE).denyAll()
				.anyMessage().authenticated();

		return msg.build();
	}
}
