package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;

/**
 * @author ws
 */
@Controller
public class Index2Conftoller {

    public static void main(String[] args) {
        String str = new BCryptPasswordEncoder().encode("nacos");
        System.out.println(str);
    }

    @Autowired
    private UserJpaDao userJpaDao;

    /*@Autowired
    private HibernateDao hibernateDao;*/


    @RequestMapping(value = "index2")
    @ResponseBody
    public Mono<String> index(ServerHttpRequest serverHttpRequest){
        System.out.println(serverHttpRequest.getId());
        System.out.println(serverHttpRequest.getMethod().name());
        //List<User> list = userJpaDao.selectUser();
        for(int i = 0; i < 100000; i++){
            User user = new User();
            user.setName("你好"+i);
            user.setPassword("世界"+i);
            //hibernateDao.insertObject(user);
            UserDetails userDetails = new UserDetails();
            userDetails.setSex(i%2==0?"男":"女");
            userDetails.setNickName("你好世界"+i);
            userDetails.setUserId(user.getId());
            //hibernateDao.insertObject(userDetails);
        }
        //Iterator<User> iterator = list.iterator();
        return Mono.just("你好世界");
    }

}
