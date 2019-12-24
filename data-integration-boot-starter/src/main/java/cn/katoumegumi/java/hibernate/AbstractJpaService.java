package cn.katoumegumi.java.hibernate;

import cn.katoumegumi.java.common.WsFieldUtils;
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

public abstract class AbstractJpaService<K ,P ,D extends JpaDao<K,P>> implements PagingService<P>{
    private D entityDao;
    @PersistenceContext
    private EntityManager em;

    protected JpaDao<K,P> getEntityDao(){
        if(entityDao == null) {
            Type type = this.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType)type;
            type = parameterizedType.getActualTypeArguments()[2];
            Field field = WsFieldUtils.getFieldByType(type,this.getClass());
            try {
                field.setAccessible(true);
                entityDao = (D)field.get(this);
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
        }
        return entityDao;
    }

    @Override
    public IPage<P> selectPage(MySearchList mySearchList) {
        Specification<P> specification = JpaDataHandle.getSpecification(mySearchList);
        Page<P> pageVO = mySearchList.getPageVO();
        if(pageVO == null){
            pageVO = new Page();
        }
        Pageable pageable = PageRequest.of(Long.valueOf(pageVO.getCurrent()-1).intValue(),Long.valueOf(pageVO.getSize()).intValue());
        org.springframework.data.domain.Page<P> tPage = getEntityDao().findAll(specification,pageable);
        if(tPage == null){
            return null;
        }
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
