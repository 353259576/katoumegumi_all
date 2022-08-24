package cn.katoumegumi.java.sql.test;

import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.common.model.WsRun;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.entity.SqlEquation;
import cn.katoumegumi.java.sql.handle.MysqlHandle;
import cn.katoumegumi.java.sql.model.SelectModel;
import cn.katoumegumi.java.sql.model.SqlConditionModel;
import cn.katoumegumi.java.sql.test.model.ChildTestBean2;
import cn.katoumegumi.java.sql.test.model.User;
import cn.katoumegumi.java.sql.test.model.UserDetails;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        System.out.println(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    public static void test() {


        SQLModelUtils.addSqlInterceptor(new AbstractSqlInterceptor() {
            @Override
            public String fieldName() {
                return "name";
            }

            @Override
            public Object selectFill() {
                return "你好世界";
            }

            @Override
            public boolean isSelect() {
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

        Long date = WsDateUtils.getExecutionTime.apply(()->{
            for (int i = 0; i < 1000000; i++){
                SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
                SelectModel selectModel = sqlModelUtils.transferToSelectModel();
                SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
                /*System.out.println(selectSqlEntity.getSelectSql());
                System.out.println(selectSqlEntity.getCountSql());
                System.out.println(new Gson().toJson(selectSqlEntity.getValueList()));
                System.out.println(sqlModelUtils.select().getSelectSql());*/

            }
        });
        System.out.println(date);
        /*SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectModel selectModel = sqlModelUtils.transferToSelectModel();
        SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
        System.out.println(selectSqlEntity.getSelectSql());
        System.out.println(new Gson().toJson(selectSqlEntity.getValueList()));
        System.out.println(sqlModelUtils.select().getSelectSql());*/
    }
}
