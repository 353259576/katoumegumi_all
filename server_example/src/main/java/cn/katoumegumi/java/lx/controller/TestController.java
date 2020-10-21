package cn.katoumegumi.java.lx.controller;

import cn.katoumegumi.java.common.SupplierFunc;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.lx.model.User;
import cn.katoumegumi.java.lx.model.UserDetails;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestController {


    public static void main(String[] args) {
        User user = new User();
        MySearchList mySearchList = MySearchList.create(User.class);
        mySearchList.setPageVO(new Page(2,20));
        mySearchList.innerJoin(UserDetails.class,t->t.setJoinTableNickName(User::getUserDetails).on(User::getId,UserDetails::getUserId));
        mySearchList.or(MySearchList.newMySearchList().eq("userDetails.sex", "男").eq(user::getName, "你好"),
                MySearchList.newMySearchList().eq(user::getPassword, "世界")
        )
                .eq(User::getId, 1)
                .lte(User::getCreateDate, "2019-12-13")
                .eqp(User::getName, User::getPassword)
                .sort("id", "ASC")
                .sort("userDetails.sex", "DESC");


        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity selectSqlEntity = sqlModelUtils.select();
        System.out.println(selectSqlEntity.getSelectSql());

        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .lambda()
                .ge(User::getId, 18);

        //System.out.println(str);


    }


}
