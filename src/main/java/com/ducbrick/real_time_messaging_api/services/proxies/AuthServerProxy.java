package com.ducbrick.real_time_messaging_api.services.proxies;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "auth-server", url = "${app.auth-server.url}")
@Validated
public interface AuthServerProxy {
	@GetMapping("${app.auth-server.userinfo-endpoint}")
	AuthServerUsrInfo getUserInfo(@NotNull @RequestParam("access_token") String accessToken);
}