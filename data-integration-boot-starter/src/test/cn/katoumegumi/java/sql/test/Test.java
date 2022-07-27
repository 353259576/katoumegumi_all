package cn.katoumegumi.java.sql.test;

import cn.katoumegumi.java.sql.AbstractSqlInterceptor;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import cn.katoumegumi.java.sql.handle.MysqlHandle;
import cn.katoumegumi.java.sql.model.SelectModel;
import cn.katoumegumi.java.sql.model.SqlConditionModel;
import cn.katoumegumi.java.sql.test.model.User;
import cn.katoumegumi.java.sql.test.model.UserDetails;
import com.google.gson.Gson;

public class Test {

    public static void main(String[] args) {
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
                .setAlias("u")
                .leftJoin(UserDetails.class,t->t.setJoinTableNickName("ud1").on(User::getId,UserDetails::getUserId))
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
        mySearchList.sort(User::getId,"asc");
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectModel selectModel = sqlModelUtils.transferToSelectModel();
        SelectSqlEntity selectSqlEntity = MysqlHandle.handleSelect(selectModel);
        System.out.println(selectSqlEntity.getSelectSql());
        System.out.println(new Gson().toJson(selectSqlEntity.getValueList()));
        System.out.println(sqlModelUtils.select().getSelectSql());
    }
}
