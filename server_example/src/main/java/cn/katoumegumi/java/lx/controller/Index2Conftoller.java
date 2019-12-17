package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

import java.util.Iterator;
import java.util.List;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/11 15:43
 */
@Controller
public class Index2Conftoller {

    public static void main(String[] args) {
        String str = new BCryptPasswordEncoder().encode("nacos");
        System.out.println(str);
    }

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
