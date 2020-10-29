package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.SupplierFunc;
import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserCC;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestController {


    public static void main(String[] args) {


        User user = new User();
        user.setId(1L);
        user.setName("你好世界");
        user.setPassword("世界你好");
        user.setCreateDate(LocalDateTime.now());
        UserDetails userDetails = new UserDetails();
        userDetails.setId(2L);
        user.setUserDetails(Collections.singletonList(userDetails));


        WsDateUtils.getExecutionTime.accept(()->{
            for(int i = 0; i < 1; i++){
                UserCC userCC = WsBeanUtils.convertBean(user, UserCC.class);
                //UserCC userCC = new UserCC();
                //BeanUtils.copyProperties(user,userCC);
                System.out.println(JSON.toJSONString(userCC));
            }
        });



        Field field = WsFieldUtils.getFieldByName(user.getClass(),"userDetails");
        System.out.println(field.getGenericType());

        MySearchList mySearchList = MySearchList.create(User.class)
                .setSqlLimit(sqlLimit -> sqlLimit.setOffset(0).setSize(10))
                .innerJoin(UserDetails.class,t->t
                        .setJoinTableNickName(User::getUserDetails)
                        .setAlias("ud")
                        .on(User::getId,UserDetails::getUserId)
                        .condition(s->s.eq(User::getName,"你好"))
                ).eq("ud",UserDetails::getSex,"1").sort(User::getName,"desc");
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity selectSqlEntity = sqlModelUtils.select();
        System.out.println(selectSqlEntity.getSelectSql());
        System.out.println(selectSqlEntity.getCountSql());
        sqlModelUtils = new SQLModelUtils(SQLModelUtils.ObjectToMySearchList(user));
        System.out.println(sqlModelUtils.update(user,true).getUpdateSql());
        sqlModelUtils = new SQLModelUtils(MySearchList.create(User.class).eq(User::getId,1));
        System.out.println(sqlModelUtils.delete().getDeleteSql());
        System.out.println();



    }


}
