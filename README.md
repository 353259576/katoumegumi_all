# ws_all
#### 目录介绍
 1. data-integration-boot-starter 一个spring的starter包，包含对于jpa和hibernate还有spring jdbc的配置
 2. server_example 工具使用示例
 3. utils 工具包，包含一个基于netty的http client还有一个兼容一部分hibernate注解和mybatis plus注解的sql生成器
 4. vertx_test vertx的练习测试
 
 ### 基本使用：
 1. 引用项目
 ```
        <dependency>
            <groupId>cn.katoumegumi.java</groupId>
            <artifactId>data-integration-boot-starter</artifactId>
            <version>1.0.3-SNAPSHOT</version>
        </dependency>
 ```
 2. yml基本配置
 ``` 
 megumi:
   datasource:
     enable: true
     druids:
       - alias: master
         url: jdbc:mysql://localhost:3306/wslx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
         username: root
         password: 199645
       - alias: slave1
         url: jdbc:mysql://localhost:3306/wslx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
         username: root
         password: 199645
     seata-enable: false
   redis:
     enable: true
     host: 127.0.0.1
     port: 6379
     defult-cache-time: 600
     caches:
       index1: 100
     #password: 199645
   hibernate:
     dialect: cn.katoumegumi.java.hibernate.ExtendedMySQLDialect
     scan-package: cn.katoumegumi.java.lx.model
     enable: true
     show-sql: true
     physical-strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
     hbm2ddl: update
     implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
     other-config:
       hibernate.globally_quoted_identifiers: true
   jpa:
     packages-to-scan: cn.katoumegumi.java.lx.model
     generate-ddl: true
     enable: true
     mapping-resources: cn.katoumegumi.java.lx.jpa
     show-sql: true
```
3. 多数据库切换使用 @DataBase(dataBaseName = "") 进行切换

4. hibernate需要使用事务使用@HibernateTransactional hibernate与jpa事务不兼容

5. jdbc hibernate jpa 使用

jdbc
```
    @Autowired
    private WsJdbcUtils jdbcUtils;

    public void test(){
        User user = new User();
        MySearchList searchList = MySearchList.create(User.class).setAlias("u");
                    searchList
                            .setPageVO(new Page(2,10))
                            .innerJoin(UserDetails.class,
                                    t -> t.setJoinTableNickName("userDetails")
                                    .setAlias("ud")
                                    .on("id","userId")
                            )
                            .innerJoin(UserDetailsRemake.class,
                                    t->t.setTableNickName("userDetails")
                                            .setJoinTableNickName("userDetails.userDetailsRemakeList")
                                            .setAlias("udr")
                                            .on("id","userDetailsId")
                            );
        IPage<User> list = jdbcUtils.getTPage(mySearchList);
    }

```
等价于
```
select `u`.`id` `u.id`,`u`.`name` `u.name`,`u`.`create_date` `u.createDate`,`u`.`password` `u.password`,`ud`.`id` `ud.id`,`ud`.`nick_name` `ud.nickName`,`ud`.`sex` `ud.sex`,`ud`.`user_id` `ud.userId`,`U_0`.`id` `U_0.id`,`U_0`.`user_details_id` `U_0.userDetailsId`,`U_0`.`remake` `U_0.remake` from `ws_user` `u`   INNER JOIN `ws_user_details` `ud` on `u`.`id` = `ud`.`user_id`  LEFT JOIN `user_details_remake` `U_0` on `ud`.`id` = `U_0`.`user_details_id`  INNER JOIN `user_details_remake` `udr` on `ud`.`id` = `udr`.`user_details_id` limit 10,10
```

hibernate
```
    @Autowired
    private HibernateDao hibernateDao;

    public void test(){
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList().sort("userDetails.id", "desc");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(user::getId, 1)
                .lte(user::getCreateDate, "2019-12-13")
                .eqp(user::getName, user::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        List<User> users = hibernateDao.selectValueToList(mySearchList, User.class);
        return JSON.toJSONString(user);
    }
```

jpa
```
@Transactional
public interface UserJpaDao extends JpaDao<Long, User> {


}

```
```
@Service
public class UserServiceImpl extends AbstractJpaService<Long, User, UserJpaDao> implements UserService {

    @Autowired
    private UserJpaDao userJpaDao;
}
```
```
public void test(){
        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList().sort("userDetails.id", "desc");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(user::getId, 1)
                .lte(user::getCreateDate, "2019-12-13")
                .eqp(user::getName, user::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        List<User> users = userService.selectList(mySearchList);
}
```


