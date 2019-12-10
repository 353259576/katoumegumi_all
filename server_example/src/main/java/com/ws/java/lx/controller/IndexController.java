package com.ws.java.lx.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ws.java.common.WsFieldUtils;
import com.ws.java.datasource.annotation.DataBase;
import com.ws.java.hibernate.HibernateDao;
import com.ws.java.hibernate.JpaDataHandle;
import com.ws.java.hibernate.MySearchList;
import com.ws.java.lx.mapper.UserMapper;
import com.ws.java.lx.model.UserDetails;
import com.ws.java.lx.service.IndexService;
import com.ws.java.lx.jpa.UserJpaDao;
import com.ws.java.lx.model.User;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HibernateDao hibernateTemplate;

    @Autowired
    private UserJpaDao userJpaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HibernateDao hibernateDao;
    @Override
    @RequestMapping(value = "index")
    @DataBase(dataBaseName = "master")
    //@Cacheable(cacheNames = "index1")
    //@GlobalTransactional
    @Transactional(rollbackFor = RuntimeException.class)
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
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList();
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex","男").eq(user::getName,"你好"),
                MySearchList.newMySearchList().eq(user::getPassword,"世界")
                ).eq(user::getId,1).sort("id","ASC").sort("userDetails.sex","DESC");
        Specification<User> specification = JpaDataHandle.<User>getSpecification(mySearchList);
        List<User> list = userJpaDao.findAll(specification);
        List<User> list1 = hibernateDao.selectValueToList(mySearchList,User.class);
        System.out.println(JSON.toJSONString(list1));

                //userJpaDao.findAll();
        //System.out.println(list.size());
        user.setName("你好");
        user.setPassword("世界");
        hibernateTemplate.insertObject(user);
        //hibernateDao.insertObject(user);
        //throw new RuntimeException("你好错误");
        String str = JSON.toJSONString(list1);
        return str;
    }


    public static void main(String[] args) {
        User user = new User();
        UserDetails userDetails = new UserDetails();
        user.setUserDetails(userDetails);
        System.out.println(WsFieldUtils.getFieldName(user.getUserDetails()::getId));
    }
}
