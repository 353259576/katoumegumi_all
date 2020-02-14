package cn.katoumegumi.java.hibernate;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Optional;

/**
 * @author ws

 */
public interface PagingService <T>  {

    public <S extends T> S save(S var1);

    public <S extends T> List<S> saveAll(Iterable<S> var1);

    public <S extends T> S saveAndFlush(S var1);

    public IPage<T> selectPage(MySearchList mySearchList);

    public List<T> selectList(MySearchList mySearchList);

}
