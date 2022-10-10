package cn.katoumegumi.java.starter.jpa.hibernate;

import cn.katoumegumi.java.sql.MySearchList;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @author ws
 */
public interface PagingService<T> {

    <S extends T> S save(S var1);

    <S extends T> List<S> saveAll(Iterable<S> var1);

    <S extends T> S saveAndFlush(S var1);

    IPage<T> selectPage(MySearchList mySearchList);

    List<T> selectList(MySearchList mySearchList);

}
