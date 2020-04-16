package cn.katoumegumi.java.lx.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ws
 */
@Component
@Slf4j
public class TestWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("当前请求连接为:{}", request.getURI().getPath());
        log.info("当前url带入参数为:{}", JSON.toJSONString(request.getQueryParams()));
        Flux<DataBuffer> flux = request.getBody();
        flux.subscribe(dataBuffer -> {
            byte bytes[] = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            System.out.println(new String(bytes));
        });
        return chain.filter(exchange);
    }
}
