package com.ws.java.lx.config;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.awt.image.DataBufferByte;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/2 16:40
 */
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges.pathMatchers("/actuator/**").permitAll()
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.opaqueToken()
                ).exceptionHandling(new Customizer<ServerHttpSecurity.ExceptionHandlingSpec>() {
            @Override
            public void customize(ServerHttpSecurity.ExceptionHandlingSpec exceptionHandlingSpec) {
                exceptionHandlingSpec.accessDeniedHandler(new ServerAccessDeniedHandler() {
                    @Override
                    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
                        denied.getCause().printStackTrace();
                        return Mono.empty();
                    }
                }).authenticationEntryPoint(new ServerAuthenticationEntryPoint() {
                    @Override
                    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
                        e.getCause().printStackTrace();
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return Mono.empty();
                    }
                });
            }
        });
        return http.build();
    }

}
