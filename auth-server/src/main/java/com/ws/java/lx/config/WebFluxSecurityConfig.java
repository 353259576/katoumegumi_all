package com.ws.java.lx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.HeaderWriterServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author ws
 * @date Created by Administrator on 2019/11/27 11:43
 */
@Configuration
@EnableWebFluxSecurity
public class WebFluxSecurityConfig {


    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder()
                .username("ws")
                .password("123456")
                .roles("admin")
                .build();
        return new MapReactiveUserDetailsService(userDetails);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity){
        /*serverHttpSecurity.authorizeExchange(new Customizer<ServerHttpSecurity.AuthorizeExchangeSpec>() {
            @Override
            public void customize(ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) {
                authorizeExchangeSpec.anyExchange().authenticated();
            }
        }).oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::opaqueToken).httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .csrf(new Customizer<ServerHttpSecurity.CsrfSpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.CsrfSpec csrfSpec) {
                        csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
                    }
                }).headers(new Customizer<ServerHttpSecurity.HeaderSpec>() {
            @Override
            public void customize(ServerHttpSecurity.HeaderSpec headerSpec) {
                headerSpec.hsts(new Customizer<ServerHttpSecurity.HeaderSpec.HstsSpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.HeaderSpec.HstsSpec hstsSpec) {
                        hstsSpec.includeSubdomains(true)
                                .preload(true)
                                .maxAge(Duration.ofDays(20));
                    }
                });
                headerSpec.frameOptions(new Customizer<ServerHttpSecurity.HeaderSpec.FrameOptionsSpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec frameOptionsSpec) {
                        frameOptionsSpec.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN);
                    }
                });
                headerSpec.xssProtection(new Customizer<ServerHttpSecurity.HeaderSpec.XssProtectionSpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.HeaderSpec.XssProtectionSpec xssProtectionSpec) {

                    }
                });
                headerSpec.referrerPolicy(new Customizer<ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec referrerPolicySpec) {
                        referrerPolicySpec.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.SAME_ORIGIN);
                    }
                });



            }
        });
        ServerLogoutHandler serverLogoutHandler = new SecurityContextServerLogoutHandler();
        ClearSiteDataServerHttpHeadersWriter clearSiteDataServerHttpHeadersWriter = new ClearSiteDataServerHttpHeadersWriter(ClearSiteDataServerHttpHeadersWriter.Directive.CACHE, ClearSiteDataServerHttpHeadersWriter.Directive.COOKIES);
        ServerLogoutHandler clearSiteData = new HeaderWriterServerLogoutHandler(clearSiteDataServerHttpHeadersWriter);
        DelegatingServerLogoutHandler logoutHandler = new DelegatingServerLogoutHandler(serverLogoutHandler, clearSiteData);
        serverHttpSecurity.logout(new Customizer<ServerHttpSecurity.LogoutSpec>() {
            @Override
            public void customize(ServerHttpSecurity.LogoutSpec logoutSpec) {
                logoutSpec.logoutHandler(logoutHandler);
            }
        });
        return serverHttpSecurity.build();*/

        serverHttpSecurity
                .authorizeExchange()
                .pathMatchers("/messages/**").hasAuthority("SCOPE_message:read")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .opaqueToken()
                .introspector(new NimbusReactiveOpaqueTokenIntrospector("http://localhost:8099/oauth/token","client","secret"));
        return serverHttpSecurity.build();
    }

    /*@Bean
    public ReactiveOpaqueTokenIntrospector introspector() {
        return new NimbusReactiveOpaqueTokenIntrospector("", clientId, clientSecret);
    }*/


    @Bean
    PasswordEncoder passwordEncoder(){
        PasswordEncoder passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                System.out.println(rawPassword);
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.equals(encodedPassword);
            }
        };
        return passwordEncoder;
    }


}
