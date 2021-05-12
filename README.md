# ws_all
 目录介绍
=================

 1. data-integration-boot-starter 一个spring的starter包，包含对于jpa和hibernate还有spring jdbc的配置
 2. utils 工具包
 
  基本使用
 =================
 ### 引用项目
 ```
        <dependency>
            <groupId>cn.katoumegumi.java</groupId>
            <artifactId>data-integration-boot-starter</artifactId>
            <version>1.0.5-SNAPSHOT</version>
        </dependency>
 ```
 ### yml基本配置
 ``` 
 megumi:
   datasource:
     enable: true
     druids:
     #多数据库配置
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
### 多数据库切换使用 @DataBase(dataBaseName = "") 进行切换

### hibernate需要使用事务使用@HibernateTransactional hibernate与jpa事务不兼容

### jdbc hibernate jpa 使用

jdbc:
1. 首选需要引用工具类，基于spring jdbc 的工具类。
```
    @Resource
    private WsJdbcUtils jdbcUtils;
```

```

     MySearchList mySearchList = MySearchList.create(User.class)
             //设置主表别名
             .setAlias("u")
             //分页设置
             .setSqlLimit(sqlLimit -> sqlLimit.setOffset(0).setSize(10))
             //内联表 支持leftJoin rightJoin innerJoin
             .innerJoin(UserDetails.class, t -> t
                     //设置关联表对应的实体位置
                     .setJoinTableNickName(User::getUserDetails)
                     //设置关联表的别名
                     .setAlias("ud")
                     //基本关联关系
                     .on(User::getId, UserDetails::getUserId)
                     //附加关联关系
                     .condition(s -> s.eq(User::getName, "你好"))
             )
             .innerJoin(UserDetailsRemake.class, t -> t
                     .setTableNickName("ud")
                     .setJoinTableNickName("ud",UserDetails::getUserDetailsRemake)
                     .on(UserDetails::getId, UserDetailsRemake::getUserDetailsId)
             )
             //in 嵌套
             .in("userDetails",UserDetails::getId,MySearchList
                     .create(UserDetailsRemake.class)
                     //限制sql语句只返回这列值，嵌套模式必须添加
                     .singleColumnName(UserDetailsRemake::getUserDetailsId)
                     .eqp("userDetailsId","User.userDetails.id")
             )
             // =
             .eq("userDetails", UserDetails::getSex, "1")
             // !=
             .ne("userDetails", UserDetails::getSex, "1")
             // is null
             .isNull(User::getName)
             // is not null
             .isNotNull(User::getName)
             // in
             .in(User::getName,Arrays.asList("1","2"))
             // not in
             .nIn(User::getName,Arrays.asList("1","2"))
             //自定义sql语句
             .sql("{u}.id = ?",Collections.singletonList(1))
             // exists
             .exists("select * from `user` u where u.id = {u}.id and u.name = ?",Collections.singletonList("你好世界"))
             // not exists
             .notExists("select * from `user` u where u.id = {u}.id and u.name = ?",Collections.singletonList("你好世界"))
             .between(User::getName,"1","2")
             .notBetween(User::getName,"1","3")
             //与表字段的=判断
             .eqp(User::getName,User::getName)
             //与表字段的>=判断
             .gtep(User::getName,User::getName)
             //与表字段的>判断
             .gtp(User::getName,User::getName)
             //与表字段的<判断
             .ltp(User::getName,User::getName)
             //与表字段的<=判断
             .ltep(User::getName,User::getName)
             // >=
             .gte(User::getName,1)
             // >
             .gt(User::getName,1)
             // <
             .lt(User::getName,1)
             // <=
             .lte(User::getName,1)
             //条件判断 当condition为true时才执行
             .condition(false,m->{
                 m.eq(User::getName,"你好世界");
             })
             .eq("userDetails.userDetailsRemake", UserDetailsRemake::getId, "1")
             //排序
             .sort(User::getName, "desc")
             //自定义的判断条件，执行复杂语句时使用
             .sqlEquation(sqlEquation -> sqlEquation.column(SqlFunction.create("IFNULL(?,0)",User::getName)).add().value(11).subtract().column(User::getPassword).equal().value(20))
             .sqlEquation(sqlEquation -> sqlEquation.sql(MySearchList.create(UserDetails.class).singleColumnName(UserDetails::getUserId).eq(UserDetails::getUserId,1)).equal().value("你好啊"));
     IPage<User> list = jdbcUtils.getTPage(mySearchList);

```
等价于
```
SELECT
	`u`.`id` `u.id`,
	`u`.`name` `u.name`,
	`u`.`create_date` `u.createDate`,
	`u`.`password` `u.password`,
	`ud`.`id` `ud.id`,
	`ud`.`nick_name` `ud.nickName`,
	`ud`.`sex` `ud.sex`,
	`ud`.`user_id` `ud.userId`,
	`U_0`.`id` `U_0.id`,
	`U_0`.`user_details_id` `U_0.userDetailsId`,
	`U_0`.`remake` `U_0.remake` 
FROM
	`ws_user` `u`
	INNER JOIN `ws_user_details` `ud` ON `u`.`id` = `ud`.`user_id` 
	AND `u`.`name` = ?
	INNER JOIN `user_details_remake` `U_0` ON `ud`.`id` = `U_0`.`user_details_id` 
WHERE
	`ud`.`id` IN ( SELECT DISTINCT `U_1`.`user_details_id` `U_1.userDetailsId` FROM `user_details_remake` `U_1` WHERE `U_1`.`user_details_id` = `ud`.`id` ) 
	AND `ud`.`sex` = ? 
	AND `ud`.`sex` != ? 
	AND `u`.`name` IS NULL 
	AND `u`.`name` IS NOT NULL 
	AND `u`.`name` IN (?,?) 
	AND `u`.`name` NOT IN (?,?) 
	AND u.id = ? 
	AND EXISTS (
	SELECT
		* 
	FROM
		`user` u 
	WHERE
		u.id = u.id 
	AND u.NAME = ?) 
	AND NOT EXISTS (
	SELECT
		* 
	FROM
		`user` u 
	WHERE
		u.id = u.id 
	AND u.NAME = ?) 
	AND `u`.`name` BETWEEN ? 
	AND ? 
	AND `u`.`name` NOT BETWEEN ? 
	AND ? 
	AND `u`.`name` = `u`.`name` 
	AND `u`.`name` >= `u`.`name` 
	AND `u`.`name` > `u`.`name` 
	AND `u`.`name` < `u`.`name` AND `u`.`name` <= `u`.`name` AND `u`.`name` >= ? AND `u`.`name` > ? 
	AND `u`.`name` < ? 
	AND `u`.`name` <= ? 
	AND `U_0`.`id` = ? 
	AND `I_2`.`name` + ? - `u`.`password` = ? 
	AND (
	SELECT DISTINCT
		`U_3`.`user_id` `U_3.userId` 
	FROM
		`ws_user_details` `U_3`
		LEFT JOIN `user_details_remake` `U_4` ON `U_3`.`id` = `U_4`.`user_details_id` 
	WHERE
	`U_3`.`user_id` = ?) = ? 
ORDER BY
	`u`.`name` DESC 
	LIMIT 0,
	10
```

hibernate
使用hibernate时，引用HibernateDao
```
    @Resource
    private HibernateDao hibernateDao;
```
```
        MySearchList mySearchList = MySearchList.newMySearchList().sort("userDetails.id", "desc");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(User::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(User::getId, 1)
                .lte(User::getCreateDate, "2019-12-13")
                .eqp(User::getName, user::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        List<User> users = hibernateDao.selectValueToList(mySearchList, User.class);
        return JSON.toJSONString(user);

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


