package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.datasource.WsJdbcUtils;
import cn.katoumegumi.java.datasource.annotation.DataBase;
import cn.katoumegumi.java.hibernate.HibernateDao;
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
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import cn.katoumegumi.java.sql.entity.SelectCallable;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WsJdbcUtils jdbcUtils;

    public static void main(String[] args) {

        User user = new User();

        MySearchList mySearchList = MySearchList.create(User.class);
        List<User> list = mysqlClientTest(mySearchList,User.class);
        System.out.println(JSON.toJSONString(list));
    }

    private static final SQLClient client = getSqlClient();

    public static SQLClient getSqlClient(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("provider_class", "cn.katoumegumi.java.vertx.sql.datasource.provider.DruidDataSourceProvider");
        jsonObject.put("url", "jdbc:mysql://127.0.0.1:3306/wslx?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true");
        jsonObject.put("user", "root");
        jsonObject.put("password", "199645");
        jsonObject.put("driver_class", "com.mysql.cj.jdbc.Driver");
        Vertx vertx = Vertx.vertx();
        SQLClient client = JDBCClient.create(vertx, jsonObject);
        return client;
    }

    public static <T> List<T> mysqlClientTest(MySearchList mySearchList,Class<T> tClass) {
        SQLClient client = getSqlClient();
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity selectSqlEntity = sqlModelUtils.select();
        String sql = selectSqlEntity.getSelectSql();
        System.out.println(sql);
        Map<Integer, Object> valueMap = selectSqlEntity.getValueMap();
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<Integer, Object> entry : valueMap.entrySet()) {
            System.out.println(entry.getKey());
            if (entry.getValue() instanceof LocalDateTime) {
                jsonArray.add(WsBeanUtils.objectToT(entry.getValue(), String.class));
            } else {
                jsonArray = jsonArray.add(entry.getValue());
            }

        }
        JsonArray finalJsonArray = jsonArray;
        SelectCallable<T> selectCallable = new SelectCallable<>();
        client.getConnection(new Handler<AsyncResult<SQLConnection>>() {
            @Override
            public void handle(AsyncResult<SQLConnection> event) {
                if (event.succeeded()) {
                    SQLConnection sqlConnection = event.result();
                    sqlConnection.queryWithParams(sql,finalJsonArray, SqlUtils.getVertxHandler(selectCallable,sqlModelUtils));
                } else {
                    event.cause().printStackTrace();
                }
            }
        });
        try {
            return selectCallable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;



    }

    @Override
    @RequestMapping(value = "index")
    @DataBase(dataBaseName = "slave1")
    @Transactional(rollbackFor = RuntimeException.class)
    @Path("/index")
    @GET
    public String index() throws Exception {


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
        MySearchList mySearchList = MySearchList.create(User.class);
        mySearchList.setPageVO(new Page(2,20));
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(user::getId, 1)
                .lte(user::getCreateDate, "2019-12-13")
                .eqp(user::getName, user::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");
        //List<User> list = userService.selectList(mySearchList);
        //List list = new ArrayList();
        long start = System.currentTimeMillis();
        IPage<User> list = jdbcUtils.getTPage(mySearchList);
        JSON.toJSONString(list);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        //list.get(0).setPassword("修改了一下");
        //System.out.println(JSON.toJSONString(list));

        //userJpaDao.findAll();
        //System.out.println(list.size());
        //User user = new User();
        //user.setName("你好hibernate");
        //user.setPassword("世界");
        //hibernateTemplate.insertObject(user);
        //hibernateDao.insertObject(user);
        //userJpaDao.save(user);
        //User user2 = new User();
        //user2.setName("你好jpa");
        //user2.setPassword("世界");
        //throw new RuntimeException("你好错误");\
        //userJpaDao.saveAndFlush(user2);

        //throw new RuntimeException("人为错误");

        return "";
    }

    @Transactional
    @RequestMapping(value = "index5")
    public String index2() {
        User user = new User();
        user.setName("你好啊");
        user.setPassword("123456");
        user.setCreateDate(LocalDateTime.now());
        userJpaDao.save(user);
        return JSON.toJSONString(user);
    }

    @ResponseBody
    @RequestMapping("index3")
    public String index3() {
        User user = new User();
        user.setName("你好");
        Page<User> page = new Page();
        page.setCurrent(1L);
        long startTime = System.currentTimeMillis();
        Consumer<Runnable> runnableSupplier = runnable -> {
            long start = System.currentTimeMillis();
            runnable.run();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        };
        runnableSupplier.accept(() -> {
            List<User> users = userMapper.selectUserList();
            return;
        });
        //List<User> users = userMapper.selectList(Wrappers.emptyWrapper());
        /*for (int i = 0; i < 1; i++){
            executor.execute(()->{
                List<User> iPage = userMapper.selectUserList();
                countDownLatch.countDown();
            });

        }
        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }*/
        long endTime = System.currentTimeMillis();
        System.out.println("使用mybatis一共花费时间为" + (endTime - startTime));
        return "你好世界";
    }

    @RequestMapping(value = "index4")
    @ResponseBody
    public String index4() {
        WsDateUtils.getExecutionTime.accept(()->{
            MySearchList mySearchList = MySearchList.create(User.class);
                    //.leftJoin(UserDetails.class,t->t.setJoinTableNickName("userDetails").setAlias("ud").on("id","userId"))
                    //.leftJoin(UserDetailsRemake.class,t->t.setTableNickName("userDetails").setJoinTableNickName("userDetails.userDetailsRemake").setAlias("udr").on("id","userDetailsId"));
                    //.leftJoin(UserDetailsRemake.class,t->t.setTableNickName("userDetails").setJoinTableNickName("userDetails.userDetailsRemake1").setAlias("udr1").on("id","userDetailsId"));
            List<User> userList = jdbcUtils.getListT(mySearchList);
            //System.out.println(JSON.toJSONString(userList));
        });
        return "成功";
    }

    @RequestMapping(value = "index6")
    @Transactional
    public String index6() {
        List<UserCC> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            //user.setId(i+1L);
            user.setName("你好啊");
            user.setPassword("世界");
            user.setCreateDate(LocalDateTime.now());
            //users.add(user);
            jdbcUtils.insert(user);
            jdbcUtils.update(user);
            jdbcUtils.update(MySearchList.newMySearchList().setMainClass(User.class).set(user::getName, "你好世界改").eq(user::getId, user.getId()));
            System.out.println(JSON.toJSONString(user));
        }
        //jdbcUtils.insert(users);
        users = jdbcUtils.getListT(MySearchList.create(User.class));
        System.out.println(JSON.toJSONString(users));
        return "完成";
    }

}
