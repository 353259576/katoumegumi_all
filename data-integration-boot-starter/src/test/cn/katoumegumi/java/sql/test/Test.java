package cn.katoumegumi.java.sql.test;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.common.model.BeanModel;
import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.entity.SqlEquation;
import cn.katoumegumi.java.sql.handle.MysqlHandle;
import cn.katoumegumi.java.sql.model.SelectModel;
import cn.katoumegumi.java.sql.model.UpdateModel;
import cn.katoumegumi.java.sql.test.model.LUser;
import cn.katoumegumi.java.sql.test.model.User;
import cn.katoumegumi.java.sql.test.model.UserDetails;
import cn.katoumegumi.java.sql.test.model.UserDetailsRemake;
import cn.katoumegumi.java.starter.jdbc.datasource.WsJdbcUtils;
import com.google.gson.Gson;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

public class Test {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {

        /*User user = new User();


        Map<String,Field> fieldMap = WsFieldUtils.getFieldMap(User.class);

        Method[] methods = User.class.getMethods();
        Map<String, Object[]> beanPropertyMap = new LinkedHashMap<>();

        for (Map.Entry<String, Field> stringFieldEntry : fieldMap.entrySet()) {
            if (Modifier.isStatic(stringFieldEntry.getValue().getModifiers())){
                continue;
            }
            Object[] objects = new Object[3];
            objects[0] = stringFieldEntry.getValue();
            beanPropertyMap.put(stringFieldEntry.getKey(),objects);
        }

        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())){
                continue;
            }
            String name = method.getName();
            if (name.startsWith("set")){
                if (name.length() == 3 || method.getParameterCount() != 1){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[2] = method;
            }else if (name.startsWith("get")){
                if (name.length() == 3 || method.getParameterCount() != 0){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(3));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[1] = method;
            }else if (name.startsWith("is")){
                if (name.length() == 2 || method.getParameterCount() != 0){
                    continue;
                }
                name = WsStringUtils.firstCharToLowerCase(name.substring(2));
                Object[] objects = beanPropertyMap.computeIfAbsent(name, n->new Object[3]);
                objects[1] = method;
            }
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Map<String,BeanPropertyModel> beanPropertyModelMap = new LinkedHashMap<>(beanPropertyMap.size());
        for (Map.Entry<String, Object[]> stringEntry : beanPropertyMap.entrySet()) {
            Object[] objects = stringEntry.getValue();
            if (objects[1] == null || (objects[0] == null && objects[2] == null)){
                continue;
            }
            Method getMethod = (Method) objects[1];
            MethodHandle getMethodHandle = lookup.unreflect(getMethod);
            Method setMethod = objects[2] == null ? null :  (Method) objects[2];
            MethodHandle setMethodHandle = objects[2] == null ? null : lookup.unreflect(setMethod);
            beanPropertyModelMap.put(stringEntry.getKey(),new BeanPropertyModel(stringEntry.getKey(),(Field) objects[0],getMethod,getMethodHandle,setMethod,setMethodHandle));
        }

        System.out.println(beanPropertyModelMap.size());*/


        //test3();

        /*DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        WsJdbcUtils jdbcUtils = new WsJdbcUtils();
        jdbcUtils.setJdbcTemplate(jdbcTemplate);
        for (int i = 0; i < 10; i++){
            User user = new User()
                    .setName("你好世界"+i)
                    .setPassword(i + "")
                    .setCreateDate(LocalDateTime.now());
            jdbcUtils.insert(user);
            for (int k = 0; k < 10; k++){
                UserDetails userDetails = new UserDetails()
                        .setUserId(user.getId())
                        .setSex(((k & 1) == 0)+"")
                        .setNickName("你好——世界"+i);
                jdbcUtils.insert(userDetails);
                for (int j = 0; j < 10; j++){
                    UserDetailsRemake userDetailsRemake = new UserDetailsRemake()
                            .setUserDetailsId(userDetails.getId())
                            .setRemake("这个是备注"+j);
                    jdbcUtils.insert(userDetailsRemake);
                }
            }
        }*/
        //test2();

        BeanModel beanModel = WsFieldUtils.createBeanModel(User.class);
        System.out.println(beanModel.toString());
        SQLModelUtils sqlModelUtils = new SQLModelUtils(MySearchList.create(User.class)
                .leftJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).on(User::getId,UserDetails::getUserId))
                .gtep("u",User::getName,"u",User::getName));
        SelectModel selectModel = sqlModelUtils.transferToSelectModel();
        SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
        System.out.println(selectSqlEntity.getSelectSql());
    }



    public static void test3(){
        long time;

        time = WsDateUtils.getExecutionTime.apply(()->{
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < 10000000; i++){
                list.add(i);
            }
        });
        System.out.println("执行了"+time+"毫秒");
        time = WsDateUtils.getExecutionTime.apply(()->{
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 10000000; i++){
                list.add(i);
            }
        });
        System.out.println("执行了"+time+"毫秒");




    }

    public static void test2(){
                /*DataSource dataSource = getDataSource();
        BaseDataSourceUtils dataSourceUtils = new BaseDataSourceUtils(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        WsJdbcUtils jdbcUtils = new WsJdbcUtils();
        jdbcUtils.setJdbcTemplate(jdbcTemplate);

        List<LUser> lUserList = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            LUser lUser = new LUser()
                    //.setId(14L)
                    .setAge(1)
                    .setName("你好")
                    .setPassword("1")
                    .setSex(1)
                    .setStatus(1);
            lUserList.add(lUser);
        }
        int row = jdbcUtils.insert(lUserList);
        System.out.println(row);
        for (LUser lUser : lUserList) {
            System.out.println(lUser.getId());
        }*/
        //System.out.println(dataSourceUtils.insert(lUser));




        //test();
        //test();

//        MySearchList mySearchList = MySearchList.create(LUser.class);
//        mySearchList/*.and(
//                m->m.eq(LUser::getName,1),
//                m->m.eq(LUser::getName,1),
//                m->m.eq(LUser::getName,1)
//        )*/.or(
//                m->m.eq(LUser::getName,1).eq(LUser::getName,1)/*,
//                m->m.eq(LUser::getName,1),
//                m->m.eq(LUser::getName,1)*/
//        ).eq(LUser::getName,1)
//                .eq(LUser::getName,1)
//        ;
//        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
//        System.out.println(MysqlHandle.handleSelect(sqlModelUtils.transferToSelectModel()).getSelectSql());
        DataSource dataSource = getDataSource();
        BaseDataSourceUtils dataSourceUtils = new BaseDataSourceUtils(dataSource);
        /*List<User> list = dataSourceUtils.selectList(
                MySearchList.create(User.class)
                        .leftJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).on(User::getId,UserDetails::getUserId))
        );
        System.out.println(list.size());*/
        long time;

        time = WsDateUtils.getExecutionTime.apply(
                ()->{
                    for (int i = 0; i < 10; i++){
                        List<User> userList = dataSourceUtils.selectList(
                                MySearchList.create(User.class)
                                        .leftJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).on(User::getId,UserDetails::getUserId))
                        );
                    }
                }
        );

        System.out.println("新合成方法消耗的时间是：" + time);

    }

    public static void test() {


        SQLModelUtils.addSqlInterceptor(new AbstractSqlInterceptor() {
            @Override
            public String fieldName() {
                return "name";
            }

            @Override
            public Object selectFill() {
                return null;
            }

            @Override
            public boolean isSelect() {
                return true;
            }

            @Override
            public boolean isInsert() {
                return true;
            }

            @Override
            public boolean isUpdate() {
                return true;
            }
        });
        MySearchList mySearchList = MySearchList.create(User.class)
                .setSqlLimit(sqlLimit -> sqlLimit.setCurrent(5).setSize(10))
                .setAlias("u")
                .leftJoin(UserDetails.class,t->t.setJoinTableNickName("ud1").on(User::getId,UserDetails::getUserId).condition(m->m.eq("ud1",UserDetails::getId,1)))
                .leftJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).setAlias("ud").on(User::getId,UserDetails::getUserId));
        mySearchList.eq(User::getName,"你好世界");
        mySearchList.eq("ud",UserDetails::getNickName,"你好世界2");
        mySearchList.eqp("ud",UserDetails::getNickName,"u",User::getName);
        mySearchList.in("ud",UserDetails::getUserId,MySearchList.create(User.class).singleColumnName(User::getId).eqp("",User::getId,"u",User::getId));
        mySearchList.and(
                m->m.eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1),
                m->m.eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1)
        );
        mySearchList.or(
                m->m.eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1),
                m->m.eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1).eq("ud",UserDetails::getId,1)
        );
        mySearchList.sqlEquation(
                sqlEquation -> sqlEquation.column(User::getName).add().column(User::getPassword).equal().column(User::getName)
        );
        mySearchList.sqlEquation(
                sqlEquation -> sqlEquation.column(User::getName).in().value(Arrays.asList("1","2"))
        );
        mySearchList.sqlEquation(
                sqlEquation -> sqlEquation.column(User::getName).add().column(new SqlEquation().column(User::getName).subtract().value(3)).equal().value(1)
        );
        mySearchList.sql(" {ud}.id23 = {u}.id433",null);
        mySearchList.isNull(User::getName);
        mySearchList.isNotNull(User::getName);
        mySearchList.sort(User::getId,"asc");
        mySearchList.sort("rand()","desc");
        mySearchList.sort("rand()","desc");

        MySearchList deleteSearchList = MySearchList.create(User.class).eq(User::getId,1);
        SQLModelUtils deleteModelUtils = new SQLModelUtils(deleteSearchList);

        DeleteSqlEntity deleteSqlEntity = MysqlHandle.handleDelete(deleteModelUtils.transferToDeleteModel());
        System.out.println(deleteSqlEntity.getDeleteSql());
        System.out.println(new Gson().toJson(deleteSqlEntity.getValueList()));


        MySearchList updateSearchList = MySearchList.create(User.class)
                .set(User::getPassword,"3213")
                .set(User::getPassword,null)
                .add(User::getId,1)
                .subtract(User::getPassword,1)
                .multiply(User::getPassword,1)
                .divide(User::getPassword,1)
                .eq(User::getId,1);
        UpdateModel updateModel = new SQLModelUtils(updateSearchList).transferToUpdateModel();
        UpdateSqlEntity updateSqlEntity = MysqlHandle.handleUpdate(updateModel);
        System.out.println(updateSqlEntity.getUpdateSql());
        System.out.println(new Gson().toJson(updateSqlEntity.getValueList()));

        Long date = WsDateUtils.getExecutionTime.apply(()->{
            for (int i = 0; i < 1; i++){
                SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
                SelectModel selectModel = sqlModelUtils.transferToSelectModel();
                SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
                System.out.println(selectSqlEntity.getSelectSql());
                System.out.println(selectSqlEntity.getCountSql());
                System.out.println(new Gson().toJson(selectSqlEntity.getValueList()));
                //System.out.println(sqlModelUtils.select().getSelectSql());

            }
        });
        System.out.println(date);
        /*SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectModel selectModel = sqlModelUtils.transferToSelectModel();
        SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
        System.out.println(selectSqlEntity.getSelectSql());
        System.out.println(new Gson().toJson(selectSqlEntity.getValueList()));
        System.out.println(sqlModelUtils.select().getSelectSql());*/


        System.out.println(
                new Gson().toJson(
                        MysqlHandle.handleSelect(new SQLModelUtils(
                                MySearchList.create(User.class)
                                        .eq(User::getName,1)
                                        .isNull(User::getName)
                                        .sql(" name = 1",null)
                                        .exists(" name = 1",null)
                                        .sql(" name = ?",Arrays.asList((Object) null))
                                        .exists(" name = ?",Arrays.asList((Object) null))
                        ).transferToSelectModel())
                )

        );
    }


    public static DataSource getDataSource(){
        String url = "jdbc:mysql://192.168.149.5:3306/wslx?useUnicode=true&characterEncoding=UTF8&rewriteBatchedStatements=true&serverTimezone=PRC&useSSL=false&allowMultiQueries=true";
        String userName = "root";
        String password = "199645";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String dataBaseName = "root";
        return HikariCPDataSourceFactory.getDataSource(url,userName,password,driverClassName);
    }
}
