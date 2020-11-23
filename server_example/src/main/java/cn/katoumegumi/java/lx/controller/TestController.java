package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserCC;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.lx.model.UserDetailsRemake;
import cn.katoumegumi.java.sql.*;

import com.alibaba.fastjson.JSON;
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




        /*User user = new User();
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

        WsDateUtils.getExecutionTime.accept(()->{
            for(int i = 0; i < 100000; i++) {
                MySearchList mySearchList = MySearchList.create(User.class)
                        .setAlias("u")
                        .setSqlLimit(sqlLimit -> sqlLimit.setOffset(0).setSize(10))
                        .innerJoin(UserDetails.class, t -> t
                                .setJoinTableNickName(User::getUserDetails)
                                .setAlias("ud")
                                .on(User::getId, UserDetails::getUserId)
                                .condition(s -> s.eq(User::getName, "你好"))
                        )
                        .innerJoin(UserDetailsRemake.class, t -> t.setTableNickName("{ud}").setJoinTableNickName("{ud}.userDetailsRemake").on(UserDetails::getId, UserDetailsRemake::getUserDetailsId))
                        .eq("ud", UserDetails::getSex, "1").sort(User::getName, "desc");
                SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
                sqlModelUtils.select();
            }
        });*/



        //lx(new int[]{3,8,12,5,24,3,1,9,27,36,43,11,6,2,7,15,25,56});
        //lx(new int[]{55,47,36,27,18,9,4,1,3,2,6});
        /*String url = "jdbc:mysql://rm-bp13t5e43e9312u79co.mysql.rds.aliyuncs.com:3306/apes_cloud_test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String userName = "yd_test";
        String password = "123456";
        String className = Driver.class.getName();
        DataSource dataSource = HikariCPDataSourceFactory.getDataSource(url,userName,password,className);
        SqlTableToBeanUtils sqlTableToBeanUtils = new SqlTableToBeanUtils(dataSource,"apes_cloud_test");
        List<SqlTableToBeanUtils.Table> tableList = sqlTableToBeanUtils.selectTables(null);
        for (SqlTableToBeanUtils.Table table:tableList){
            List<SqlTableToBeanUtils.Column> columnList = sqlTableToBeanUtils.selectTableColumns(table.getTableName());
            columnList.forEach(column -> {
                System.out.printf("@Column(name=\"%s\")\n",column.getColumnName());
                System.out.printf("private %s %s;%n",column.getColumnClass().getSimpleName(),column.getBeanFieldName());
            });
        }*/


        WsDateUtils.getExecutionTime.accept(()->{
            for(int i = 0; i < 100000; i++){
                WsBeanUtils.objectToT(100,String.class);
            }
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
