package cn.katoumegumi.java.lx.config;

import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
/*import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;*/
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * @author ws
 */
//@EnableWebFluxSecurity
public class SecurityConfig {

    /*@Bean
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
                        ServerHttpResponse serverHttpResponse = exchange.getResponse();
                        DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap("非法访问".getBytes());
                        return serverHttpResponse.writeWith(Mono.just(dataBuffer));
                    }
                }).authenticationEntryPoint(new ServerAuthenticationEntryPoint() {
                    @Override
                    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
                        e.getCause().printStackTrace();
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        ServerHttpResponse serverHttpResponse = exchange.getResponse();
                        DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap("非法访问".getBytes());
                        return serverHttpResponse.writeWith(Mono.just(dataBuffer));
                        //return Mono.empty();
                    }
                });
            }
        });
        return http.build();
    }*/


   /*@Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges.pathMatchers("/actuator/**").permitAll()
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer
                                .authenticationEntryPoint((exchange, e) -> {
                                    e.printStackTrace();
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap("非法访问".getBytes())));
                                })
                                .accessDeniedHandler((exchange, denied) -> {
                                    denied.getCause().printStackTrace();
                                    ServerHttpResponse response = exchange.getResponse();
                                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return response.writeWith(Mono.just(response.bufferFactory().wrap("非法访问".getBytes())));
                                })
                                .opaqueToken(opaqueTokenSpec -> {
                                })
                ).exceptionHandling(exceptionHandlingSpec -> {
            exceptionHandlingSpec.authenticationEntryPoint((exchange, e) -> {
                e.getCause().printStackTrace();
                ServerHttpResponse serverHttpResponse = exchange.getResponse();
                DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap("非法访问".getBytes());
                return serverHttpResponse.writeWith(Mono.just(dataBuffer));
            }).accessDeniedHandler((exchange, denied) -> {
                denied.getCause().printStackTrace();
                ServerHttpResponse serverHttpResponse = exchange.getResponse();
                DataBuffer dataBuffer = serverHttpResponse.bufferFactory().wrap("非法访问".getBytes());
                return serverHttpResponse.writeWith(Mono.just(dataBuffer));
            });
        });
        return http.build();
    }*/



}
