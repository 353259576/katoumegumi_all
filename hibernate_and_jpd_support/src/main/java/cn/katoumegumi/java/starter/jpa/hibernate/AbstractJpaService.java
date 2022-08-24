package cn.katoumegumi.java.starter.jpa.hibernate;

import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class AbstractJpaService<K, P, D extends JpaDao<K, P>> implements PagingService<P> {
    private D entityDao;
    @PersistenceContext
    private EntityManager em;

    protected JpaDao<K, P> getEntityDao() {
        if (entityDao == null) {
            Type type = this.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) type;
            type = parameterizedType.getActualTypeArguments()[2];
            List<Field> fieldList = WsFieldUtils.getFieldByType(type, this.getClass());
            try {
                Field field = fieldList.get(0);
                field.setAccessible(true);
                entityDao = (D) field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return entityDao;
    }


    @Override
    public <S extends P> S save(S var1) {
        return getEntityDao().save(var1);
    }

    @Override
    public <S extends P> List<S> saveAll(Iterable<S> var1) {
        return getEntityDao().saveAll(var1);
    }

    @Override
    public <S extends P> S saveAndFlush(S var1) {
        return getEntityDao().saveAndFlush(var1);
    }

    @Override
    public IPage<P> selectPage(MySearchList mySearchList) {
        Specification<P> specification = JpaDataHandle.getSpecification(mySearchList);
        SqlLimit sqlLimit = mySearchList.getSqlLimit();
        if (sqlLimit == null) {
            sqlLimit = new SqlLimit();
        }
        Pageable pageable = PageRequest.of(Math.toIntExact(sqlLimit.getCurrent()), Long.valueOf(sqlLimit.getSize()).intValue());
        org.springframework.data.domain.Page<P> tPage = getEntityDao().findAll(specification, pageable);
        return convertPage(tPage);
    }


    public IPage<P> convertPage(org.springframework.data.domain.Page<P> tPage) {
        if (tPage == null) {
            return null;
        }
        Page<P> pageVO = new Page<>();
        pageVO.setCurrent(tPage.getNumber());
        pageVO.setSize(tPage.getSize());
        pageVO.setRecords(tPage.getContent());
        pageVO.setTotal(tPage.getTotalElements());
        return pageVO;
    }

    @Override
    public List<P> selectList(MySearchList mySearchList) {
        Specification<P> specification = JpaDataHandle.getSpecification(mySearchList);
        return getEntityDao().findAll(specification);
    }
}
