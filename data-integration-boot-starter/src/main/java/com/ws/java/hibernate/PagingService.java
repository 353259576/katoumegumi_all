package com.ws.java.hibernate;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/17 9:39
 */
public interface PagingService <T>  {

    public IPage<T> selectPage(MySearchList mySearchList);


    public List<T> selectList(MySearchList mySearchList);

}
