package com.ws.java.lx.controller;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ws
 * @date Created by Administrator on 2019/11/28 9:48
 */
@RestController
public class OAuth2ResourceServerController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2ResourceServerProperties.Opaquetoken opaquetoken) {
        return "你好世界";
    }

    @GetMapping("/message")
    public String message() {
        return "secret message";
    }

    @PostMapping("/message")
    public String createMessage(@RequestBody String message) {
        return String.format("Message was created. Content: %s", message);
    }
}
