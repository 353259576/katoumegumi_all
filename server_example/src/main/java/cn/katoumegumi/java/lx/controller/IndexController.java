package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.datasource.WsJdbcUtils;
import cn.katoumegumi.java.datasource.annotation.DataBase;
import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.hibernate.HibernateTransactional;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseTask;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.mapper.UserMapper;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserCC;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.lx.service.IndexService;
import cn.katoumegumi.java.lx.service.UserService;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.PageConvertUtil;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import cn.katoumegumi.java.sql.entity.SelectCallable;
import cn.katoumegumi.java.utils.PageConvertUtils;
import cn.katoumegumi.java.vertx.sql.utils.SqlUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import org.apache.dubbo.config.annotation.Service;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

@Service(version = "1.0.0", protocol = {"dubbo", "rest"})
@RestController
//@Component
@Path("/")
public class IndexController implements IndexService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier(value = "hibernateTransactionManager")
    private PlatformTransactionManager platformTransactionManager;


    @Autowired
    private UserJpaDao userJpaDao;

    @Autowired
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private HibernateDao hibernateDao;


    /*@Autowired
    private EntityManager entityManager;*/

    @Autowired
    private WsJdbcUtils jdbcUtils;


    @RequestMapping(value = "insertUser")
    @Transactional(rollbackFor = RuntimeException.class)
    public String insertUser(){
        List<User> users = new ArrayList<>();
        LocalDateTime date = LocalDateTime.now();
        for(int i = 0; i < 100;i++){
            User user = new User();
            user.setName("测试"+i);
            user.setName(WsStringUtils.createRandomStr());
            user.setCreateDate(date);
            users.add(user);
        }
        jdbcUtils.insert(users);

        Random random = new Random();

        List<UserDetails> userDetails = new ArrayList<>();
        for(User user:users){

            for(int i = 0; i < 1000; i++){
                UserDetails details = new UserDetails();
                details.setUserId(user.getId());
                details.setNickName(i+"测试名称");
                details.setSex(random.nextBoolean()?"男":"女");
                userDetails.add(details);
            }
        }
        jdbcUtils.insert(userDetails);


        for(UserDetails details:userDetails){
            List<UserDetailsRemake> remakeList = new ArrayList<>();
            for(int i = 0; i < 10; i++){
                UserDetailsRemake remake = new UserDetailsRemake();
                remake.setRemake("备注一下"+details.getId()+"_"+i);
                remake.setUserDetailsId(details.getId());
                remakeList.add(remake);
            }
            jdbcUtils.insert(remakeList);
        }

        return "你好世界";
    }


    /**
     * sqlUtils查询
     * @return
     * @throws Exception
     */
    @Override
    @GetMapping("index")
    public String index() throws Exception {
        WsDateUtils.getExecutionTime.accept(()->{
            List<User> list = jdbcUtils.getListT(MySearchList.create(User.class).leftJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).on(User::getId,UserDetails::getUserId)));
            System.out.println(list.size());
        });
        return "你好啊";
    }

    /**
     * mybatis查询
     * @return
     * @throws Exception
     */
    @GetMapping("index2")
    public String index2() throws Exception {
        WsDateUtils.getExecutionTime.accept(()->{
            List<User> list = userMapper.selectUserList();
        });
        return "你好啊";
    }

    @GetMapping("index3")
    @HibernateTransactional(rollbackFor = RuntimeException.class)
    public String index3(){
        WsDateUtils.getExecutionTime.accept(()->{
            List<User> list = hibernateDao.selectValueToList(MySearchList.create(User.class),User.class);
            System.out.println(list.size());
        });
        return "你好啊";
    }

}
