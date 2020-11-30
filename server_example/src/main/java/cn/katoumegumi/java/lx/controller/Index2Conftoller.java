package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.datasource.annotation.DataBase;
import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.hibernate.HibernateTransactional;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.lx.service.UserService;
import cn.katoumegumi.java.sql.MySearchList;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ws
 */
@Controller
public class Index2Conftoller {

    @Autowired
    private UserJpaDao userJpaDao;
    @Autowired
    private HibernateDao hibernateDao;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "hibernateTest")
    @ResponseBody
    public String hibernateTest() {
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList().sort("userDetails.id", "desc");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(user::getId, 1)
                .lte(user::getCreateDate, "2019-12-13")
                .eqp(User::getName, User::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        List<User> users = userService.selectList(mySearchList);
        return JSON.toJSONString(user);
    }


    @RequestMapping(value = "index2")
    @ResponseBody
    @HibernateTransactional
    @DataBase(dataBaseName = "")
    public Mono<String> index(ServerHttpRequest serverHttpRequest) {
        System.out.println(serverHttpRequest.getId());
        System.out.println(serverHttpRequest.getMethod().name());
        //List<User> list = userJpaDao.selectUser();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("你好" + i);
            user.setPassword("世界" + i);
            user.setCreateDate(LocalDateTime.now());
            hibernateDao.insertObject(user);
            for (int k = 0; k < 10; k++) {
                UserDetails userDetails = new UserDetails();
                userDetails.setSex(k % 2 == 0 ? "男" : "女");
                userDetails.setNickName("你好世界" + i);
                userDetails.setUserId(user.getId());
                hibernateDao.insertObject(userDetails);
                for (int j = 0; j < 10; j++) {
                    UserDetailsRemake userDetailsRemake = new UserDetailsRemake();
                    userDetailsRemake.setRemake(j + "");
                    userDetailsRemake.setUserDetailsId(userDetails.getId());
                    hibernateDao.insertObject(userDetailsRemake);
                }
            }

            //hibernateDao.insertObject(userDetails);
        }
        //Iterator<User> iterator = list.iterator();
        return Mono.just("你好世界");
    }

}
