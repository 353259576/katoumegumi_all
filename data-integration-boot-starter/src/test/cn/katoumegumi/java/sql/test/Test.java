package cn.katoumegumi.java.sql.test;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsUnsafeUtils;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.entity.SqlEquation;
import cn.katoumegumi.java.sql.handle.MysqlHandle;
import cn.katoumegumi.java.sql.model.SelectModel;
import cn.katoumegumi.java.sql.model.UpdateModel;
import cn.katoumegumi.java.sql.test.model.LUser;
import cn.katoumegumi.java.sql.test.model.User;
import cn.katoumegumi.java.sql.test.model.UserDetails;
import cn.katoumegumi.java.sql.test.model.UserDetailsRemake;
import com.google.gson.Gson;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Test {

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {

        User user = new User();

        Method[] methods = User.class.getMethods();

        List<Method> list = new ArrayList<>();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set")){
                list.add(method);
            }else if (name.startsWith("get")){
                list.add(method);
            }else if (name.startsWith("is")){
                list.add(method);
            }else {
                continue;
            }
        }
        for (Method method : list) {
            System.out.println(method.getName());
        }


        //test3();
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
                        dataSourceUtils.selectList(
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
        String url = "jdbc:mysql://localhost:3306/wslx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        String userName = "root";
        String password = "199645";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String dataBaseName = "root";
        return HikariCPDataSourceFactory.getDataSource(url,userName,password,driverClassName);
    }
}
