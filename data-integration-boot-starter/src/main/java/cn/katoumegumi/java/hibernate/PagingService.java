package cn.katoumegumi.java.hibernate;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author ws

 */
public interface PagingService <T>  {

    public IPage<T> selectPage(MySearchList mySearchList);


    public List<T> selectList(MySearchList mySearchList);

}
