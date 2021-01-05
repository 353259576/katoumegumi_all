package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserCC;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.sql.*;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.jdbc.Driver;
import io.vertx.core.json.Json;
import org.springframework.beans.BeanUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestController {


    public static void main(String[] args) {



        LambdaQueryWrapper lambdaQueryWrapper = Wrappers.lambdaQuery();

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

        //MySearchList searchList = MySearchList.create(User.class);
        //new SQLModelUtils(searchList).select();

        WsDateUtils.getExecutionTime.accept(()->{
            for(int i = 0; i < 1; i++) {
                MySearchList mySearchList = MySearchList.create(User.class)
                        .setAlias("u")
                        .setSqlLimit(sqlLimit -> sqlLimit.setOffset(0).setSize(10))
                        .innerJoin(UserDetails.class, t -> t
                                .setJoinTableNickName(User::getUserDetails)
                                .setAlias("ud")
                                .on(User::getId, UserDetails::getUserId)
                                .condition(s -> s.eq(User::getName, "你好"))
                        )
                        .innerJoin(UserDetailsRemake.class, t -> t.setTableNickName("ud").setJoinTableNickName("ud",UserDetails::getUserDetailsRemake).on(UserDetails::getId, UserDetailsRemake::getUserDetailsId))
                        .in("userDetails",UserDetails::getId,MySearchList.create(UserDetailsRemake.class).singleColumnName(UserDetailsRemake::getUserDetailsId).eqp("userDetailsId","User.userDetails.id"))
                        .eq("userDetails", UserDetails::getSex, "1")
                        .ne("userDetails", UserDetails::getSex, "1")
                        .isNull(User::getName)
                        .isNotNull(User::getName)
                        .in(User::getName,Arrays.asList("1","2"))
                        .nIn(User::getName,Arrays.asList("1","2"))
                        .exists("234234",Collections.singletonList("1"))
                        .notExists("234234",Collections.singletonList("1"))
                        .between(User::getName,"1","2")
                        .notBetween(User::getName,"1","3")
                        .eqp(User::getName,User::getName)
                        .gtep(User::getName,User::getName)
                        .gtp(User::getName,User::getName)
                        .ltp(User::getName,User::getName)
                        .ltep(User::getName,User::getName)
                        .gte(User::getName,1)
                        .gt(User::getName,1)
                        .lt(User::getName,1)
                        .lte(User::getName,1)
                        .eq("userDetails.userDetailsRemake", UserDetailsRemake::getId, "1").sort(User::getName, "desc");
                SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
                SelectSqlEntity entity = sqlModelUtils.select();
                System.out.println(entity.getSelectSql());
                System.out.println(JSON.toJSONString(entity.getValueList()));
            }
        });


        WsDateUtils.getExecutionTime.accept(()->{

            MySearchList mySearchList = MySearchList.create(User.class);
            mySearchList.set(User::getName,"你好")
                    .add(User::getName,1)
                    .subtract(User::getName,2)
                    .multiply(User::getName,3)
                    .divide(User::getName,4)
                    .eq(User::getName,5);
            SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
            UpdateSqlEntity updateSqlEntity = sqlModelUtils.update(mySearchList);
            System.out.println(updateSqlEntity.getUpdateSql());
            System.out.println(JSON.toJSONString(updateSqlEntity.getValueList()));

        });


    }





    public static void lx(int[] ints){

        Arrays.sort(ints);

        int[] ints1 = new int[]{0,0,0};

        List<Integer> list1 = new ArrayList<>();

        List<Integer> list2 = new ArrayList<>();

        List<Integer> list3 = new ArrayList<>();

        int l;
        int index;
        for(int i = ints.length - 1; i > -1; i--){
            l = ints[i];
            index = min(ints1[0],ints1[1],ints1[2]);
            ints1[index] = ints1[index] + l;
            switch (index){
                case 0:list1.add(l);break;
                case 1:list2.add(l);break;
                case 2:list3.add(l);break;
            }
        }
        System.out.println(JSON.toJSONString(list1));
        System.out.println(list1.stream().mapToInt(o->o).sum());
        System.out.println(JSON.toJSONString(list2));
        System.out.println(list2.stream().mapToInt(o->o).sum());
        System.out.println(JSON.toJSONString(list3));
        System.out.println(list3.stream().mapToInt(o->o).sum());

    }


    public static int min(int x,int y,int z){
        if(x <= y && x <= z){
            return 0;
        }
        if(y <= x && y <=z){
            return 1;
        }
        return 2;

    }

}
