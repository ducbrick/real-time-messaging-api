package com.ducbrick.real_time_messaging_api.services.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "auth-server", url = "${app.auth-server.url}")
public interface AuthServerProxy {
	@GetMapping("${app.auth-server.userinfo-endpoint}")
	Map<String, String> getUserInfo(@RequestParam("access_token") String accessToken);
}