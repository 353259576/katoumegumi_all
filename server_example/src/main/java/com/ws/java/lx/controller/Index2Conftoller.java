package com.ws.java.lx.controller;

import com.ws.java.lx.jpa.UserJpaDao;
import com.ws.java.lx.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/11 15:43
 */
@Controller
public class Index2Conftoller {


    @Autowired
    private UserJpaDao userJpaDao;


    @RequestMapping(value = "index2")
    @ResponseBody
    public Flux<User> index(ServerHttpRequest serverHttpRequest){
        System.out.println(serverHttpRequest.getId());
        System.out.println(serverHttpRequest.getMethod().name());
        List<User> list = userJpaDao.selectUser();
        Iterator<User> iterator = list.iterator();
        return Flux.fromArray(list.toArray(new User[list.size()]));
    }

}
