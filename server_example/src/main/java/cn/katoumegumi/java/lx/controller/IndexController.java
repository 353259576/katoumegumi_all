package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.datasource.annotation.DataBase;
import cn.katoumegumi.java.hibernate.HibernateDao;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.lx.mapper.UserMapper;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.service.UserService;
import cn.katoumegumi.java.sql.SQLModelUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.lx.service.IndexService;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.*;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service(version = "1.0.0",protocol = {"dubbo","rest"})
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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private HibernateDao hibernateDao;

    @Autowired
    private EntityManager entityManager;


    @Override
    @RequestMapping(value = "index")
    @DataBase(dataBaseName = "slave1")
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
        long start = System.currentTimeMillis();
        List<User> list = userService.selectList(mySearchList);
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
    public String index2(){
        User user = new User();
        user.setName("你好啊");
        user.setPassword("123456");
        user.setCreateDate(LocalDateTime.now());
        userJpaDao.save(user);
        return JSON.toJSONString(user);
    }


    @ResponseBody
    @RequestMapping("index3")
    public String index3(){
        User user = new User();
        user.setName("你好");
        Page<User> page = new Page();
        page.setCurrent(1L);
        Executor executor = Executors.newFixedThreadPool(8);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1; i++){
            executor.execute(()->{
                List<User> iPage = userMapper.selectUserList();
                countDownLatch.countDown();
            });

        }
        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("使用mybatis一共花费时间为"+(endTime - startTime));
        return "你好世界";
    }


    public static void main(String[] args) {
        /*User user = new User();
        UserDetails userDetails = new UserDetails();
        user.setUserDetails(userDetails);
        System.out.println(WsFieldUtils.getFieldName(user.getUserDetails()::getId));*/

        User user = new User();
        MySearchList mySearchList = MySearchList.newMySearchList().setMainClass(User.class)
                .join(null,UserDetails.class,"userDetails1","id","userId");
        //mySearchList/*.join(null,UserDetails.class,"UserDetails1","id","userId")*/
                //.join("userDetails",UserDetailsRemake.class,"userDetailsRemake","id","userDetailsId");
                //.eq("userDetails.sex","男").sort("id","desc");
        /*mySearchList.join("",UserDetails.class,"userDetails11","id","userId");
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex","男").eq(user::getName,"你好"),
                MySearchList.newMySearchList().eq(user::getPassword,"世界")
        )
                .eq(user::getId,1)
                .eq("userDetails11.sex","男")
                .lte(user::getCreateDate,"2019-12-13")
                .sort("id","ASC")
                .sort("userDetails.sex","DESC");*/
        /*Page page = new Page();
        page.setCurrent(1);
        page.setSize(10);
        mySearchList.setPageVO(page);*/
        /*long startTIme = System.currentTimeMillis();
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        String str = sqlModelUtils.searchListBaseSQLProcessor();
        long endTime = System.currentTimeMillis();
        System.out.println("初次解析sql语句花费时间为："+(endTime - startTIme));
        System.out.println(str);
        str = sqlModelUtils.searchListBaseCountSQLProcessor();
        System.out.println(str);
        long start = System.nanoTime();
        SQLModelUtils modelUtils = new SQLModelUtils(mySearchList);
        str = modelUtils.searchListBaseSQLProcessor();
        long end = System.nanoTime();
        mysqlClientTest(mySearchList);
        System.out.println(str);
        System.out.println(end - start);

        UserDetails userDetails = new UserDetails();

        mySearchList = MySearchList.newMySearchList().setMainClass(UserDetails.class);
        mySearchList.or(MySearchList.newMySearchList().eq("sex","男"), MySearchList.newMySearchList().eq(userDetails::getNickName,"世界"));
        sqlModelUtils = new SQLModelUtils(mySearchList);
        System.out.println(sqlModelUtils.searchListBaseSQLProcessor());*/
        MySearchList mySearchList1 = MySearchList.newMySearchList();
        mySearchList1.setMainClass(User.class)
                .eq(user::getName,"你好");
        SQLModelUtils sqlModelUtils1 = new SQLModelUtils(mySearchList1);
        String str = sqlModelUtils1.searchListBaseSQLProcessor();
        String countStr = sqlModelUtils1.searchListBaseCountSQLProcessor();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++){
            mySearchList1 = MySearchList.newMySearchList();
            mySearchList1.setMainClass(User.class)
                    .join(null,UserDetails.class,"UserDetails","id","userId")
                    .eq(user::getName,"你好");
            sqlModelUtils1 = new SQLModelUtils(mySearchList1);
            sqlModelUtils1.getValueMap();
            str = sqlModelUtils1.searchListBaseSQLProcessor();
            countStr = sqlModelUtils1.searchListBaseCountSQLProcessor();

        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        System.out.println(str);
        System.out.println(countStr);


    }







    public static void mysqlClientTest(MySearchList mySearchList){
        /*Vertx vertx = VertUtils.vertx;
        MySQLConnectOptions mySQLConnectOptions = new MySQLConnectOptions();
        mySQLConnectOptions.setHost("localhost");
        mySQLConnectOptions.setPort(3360);
        mySQLConnectOptions.setUser("root");
        mySQLConnectOptions.setPassword("199645");
        mySQLConnectOptions.setDatabase("pigxx");
        mySQLConnectOptions.setCharset("utf8mb4");
        mySQLConnectOptions.setSsl(false);

        Map<String,String> map = new HashMap<>();
        map.put("zeroDateTimeBehavior","convertToNull");
        map.put("useJDBCCompliantTimezoneShift","true");
        map.put("useLegacyDatetimeCode","false");
        map.put("serverTimezone","GMT%2B8");
        map.put("allowMultiQueries","true");
        map.put("allowPublicKeyRetrieval","true");
        mySQLConnectOptions.setProperties(map);
        PoolOptions poolOptions = new PoolOptions();

        MySQLPool mySQLPool = MySQLPool.pool(vertx,mySQLConnectOptions,poolOptions);

        mySQLPool.getConnection(new Handler<AsyncResult<SqlConnection>>() {
            @Override
            public void handle(AsyncResult<SqlConnection> event) {
                if(event.succeeded()){
                    SqlConnection sqlConnection = event.result();
                    sqlConnection.query("select * from sys_user", new Handler<AsyncResult<RowSet<Row>>>() {
                        @Override
                        public void handle(AsyncResult<RowSet<Row>> event) {
                            if(event.succeeded()){
                                RowSet<Row> rows = event.result();
                                System.out.println(rows.rowCount());
                                System.out.println(JSON.toJSONString(rows.columnsNames()));
                            }else {
                                event.cause().printStackTrace();
                            }
                        }
                    });
                }else {
                    event.cause().printStackTrace();
                }
            }
        });*/
        /*try {
            Thread.sleep(30000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }*/

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("provider_class","cn.katoumegumi.java.vertx.DruidDataSourceProvider");
        jsonObject.put("url","jdbc:mysql://127.0.0.1:3306/wslx?characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT%2B8&allowMultiQueries=true&allowPublicKeyRetrieval=true");
        jsonObject.put("user","root");
        jsonObject.put("password","199645");
        jsonObject.put("driver_class","com.mysql.cj.jdbc.Driver");


        Vertx vertx = Vertx.vertx();
        SQLClient client = JDBCClient.createNonShared(vertx, jsonObject);
        long timeStart = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for(int i = 0; i < 1; i++){
            int finalI = i;
            long startTime = System.currentTimeMillis();
            SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
            String sql = sqlModelUtils.searchListBaseSQLProcessor();
            System.out.println(sql);
            Map<Integer,Object> valueMap = sqlModelUtils.getValueMap();
            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<Integer,Object> entry:valueMap.entrySet()){
                System.out.println(entry.getKey());
                if(entry.getValue() instanceof LocalDateTime){
                    jsonArray.add(WsBeanUtis.objectToT(entry.getValue(),String.class));
                }else {
                    jsonArray = jsonArray.add(entry.getValue());
                }

            }
            JsonArray finalJsonArray = jsonArray;
            long endTime = System.currentTimeMillis();
            System.out.println("解析sql语句花费时间：" + (endTime - startTime));
            client.getConnection(new Handler<AsyncResult<SQLConnection>>() {
                @Override
                public void handle(AsyncResult<SQLConnection> event) {
                    if (event.succeeded()){
                        SQLConnection sqlConnection = event.result();
                        sqlConnection.queryWithParams(sql, finalJsonArray,new Handler<AsyncResult<ResultSet>>() {
                            @Override
                            public void handle(AsyncResult<ResultSet> event) {
                                if(event.succeeded()){
                                    long start = System.currentTimeMillis();
                                    ResultSet resultSet = event.result();
                                    System.out.println(resultSet.getNumRows());
                                    List<JsonObject> list = resultSet.getRows();
                                    List<Map> maps = new ArrayList<>();
                                    for(JsonObject o:list){
                                        Map map = o.getMap();
                                        maps.add(map);
                                        //User user = SQLModelUtils.loadingObject(User.class, (Map<String, Object>) map);
                                        //System.out.println(JSON.toJSONString(user));

                                    }
                                    long startTime =System.currentTimeMillis();
                                    maps = sqlModelUtils.handleMap(maps);
                                    long endTime = System.currentTimeMillis();
                                    System.out.println("处理数据花费时间:"+(endTime - startTime));
                                    startTime = System.currentTimeMillis();
                                    maps = sqlModelUtils.mergeMapList(maps);
                                    endTime = System.currentTimeMillis();
                                    System.out.println("合并数据："+(endTime - startTime));
                                    startTime = System.currentTimeMillis();
                                    List<User> users = sqlModelUtils.loadingObject(maps);
                                    endTime = System.currentTimeMillis();
                                    System.out.println("将map转换为对象花了："+ (endTime - startTime));
                                    sqlConnection.close();
                                    long end = System.currentTimeMillis();
                                    System.out.println("线程"+ finalI+"完成，共耗时："+(end -  start) +"毫秒，User数组大小为:"+users.size());
                                    countDownLatch.countDown();
                                }else {
                                    event.cause().printStackTrace();
                                }
                            }
                        });

                    }else {
                        event.cause().printStackTrace();
                    }
                }
            });
        }

        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("总共花费时间为："+(end - timeStart));






    }





}
