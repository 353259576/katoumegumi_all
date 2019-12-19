package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.datasource.annotation.DataBase;
import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.hibernate.HibernateTransactional;
import cn.katoumegumi.java.hibernate.MySearchList;
import cn.katoumegumi.java.lx.mapper.UserMapper;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.service.UserService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.lx.service.IndexService;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service(version = "1.0.0",protocol = {"dubbo","rest"})
@RestController
//@Component
@Path("/")
public class IndexController implements IndexService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;


    @Autowired
    private UserJpaDao userJpaDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HibernateDao hibernateDao;


    @Override
    @RequestMapping(value = "index")
    @DataBase(dataBaseName = "master")
    //@Cacheable(cacheNames = "index1")
    //@GlobalTransactional
    @HibernateTransactional(rollbackFor = RuntimeException.class)
    @Path("/index")
    @GET
    public String index() throws Exception{
        //CompositeHealthIndicator
        //ResteasyDeploymentImpl
        //ResteasyDeployment
        /*List<Map> list = jdbcTemplate.query("select  * from ym_user", new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet resultSet, int i) throws SQLException {
                Map map = new HashMap();
                Integer count = resultSet.getMetaData().getColumnCount();
                for(int k = 1; k <= count; k++){
                    map.put(resultSet.getMetaData().getColumnName(k),resultSet.getObject(k));
                }
                return map;
            }
        });*/
        /*List<User> list = hibernateTemplate.findByExample(new User());
        User user = new User();
        user.setName("你好");
        user.setPassword("世界");
        hibernateTemplate.saveOrUpdate(user);*/
        //List<User> list = userJpaDao.selectUser();
        LocalDateTime localDateTime = LocalDateTime.now();
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex","男").eq(user::getName,"你好"),
                MySearchList.newMySearchList().eq(user::getPassword,"世界")
                )
                .eq(user::getId,1)
                .lte(user::getCreateDate,"2019-12-13")
                .sort("id","ASC")
                .sort("userDetails.sex","DESC");
        //List<User> list = userService.selectList(mySearchList);
        //List list = new ArrayList();
        List<User> list = hibernateDao.selectValueToList(mySearchList,User.class);
        list.get(0).setPassword("修改了一下");
        System.out.println(JSON.toJSONString(list));

                //userJpaDao.findAll();
        //System.out.println(list.size());
        user.setName("你好hibernate");
        user.setPassword("世界");
        //hibernateTemplate.insertObject(user);
        //hibernateDao.insertObject(user);
        User user2 = new User();
        user2.setName("你好jpa");
        user2.setPassword("世界");
        //throw new RuntimeException("你好错误");\
        //userJpaDao.saveAndFlush(user2);
        Iterator<User> iterator = list.iterator();
        Flux.<User>create(userFluxSink -> {
            while (iterator.hasNext()){
                userFluxSink.next(iterator.next());
            }
            userFluxSink.complete();
        }).map(user1 -> {
            System.out.println(JSON.toJSONString(user1));
            return user1;
        }).subscribe(user1 -> {
            System.out.println("?");
        });
        String str = JSON.toJSONString(list);
        //throw new RuntimeException("人为错误");
        return str;
    }


    public static void main(String[] args) {
        User user = new User();
        UserDetails userDetails = new UserDetails();
        user.setUserDetails(userDetails);
        System.out.println(WsFieldUtils.getFieldName(user.getUserDetails()::getId));
    }
}
