package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.hibernate.JpaDataHandle;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ws
 */
public class PageConvertUtils {



    public static <T,P> Page<T> copyPage(Page<P> pPage,Class<T> tClass){
        Page<T> tPage = new Page<>();
        tPage.setCurrent(pPage.getCurrent());
        tPage.setSize(pPage.getSize());
        tPage.setTotal(pPage.getTotal());
        if(WsListUtils.isNotEmpty(pPage.getRecords())){
            List<P> pList = pPage.getRecords();
            List<T> tList = new ArrayList<>(pList.size());
            for(P p:pList){
                T t = WsBeanUtils.createObject(tClass);
                assert t != null;
                BeanUtils.copyProperties(p,t);
                tList.add(t);
            }
            tPage.setRecords(tList);
        }
        return tPage;
    }

    public static <T> IPage<T> createEmptyPage(IPage<?> oPage){
        Page<T> tPage = new Page<>();
        tPage.setCurrent(oPage.getCurrent());
        tPage.setTotal(oPage.getTotal());
        tPage.setSize(oPage.getSize());
        return tPage;
    }


    public static <T> Page<T> convertSpringPage(Pageable pageable) {
        Page<T> page = new Page();
        page.setSize(pageable.getPageSize());
        page.setCurrent(page.getCurrent());
        return page;
    }

    public static <T, K extends JpaSpecificationExecutor> IPage<T> createPageInfo(K jpaSpecificationExecutor, MySearchList mySearchList) {
        Specification<T> specification = JpaDataHandle.<T>getSpecification(mySearchList);
        SqlLimit sqlLimit = mySearchList.getSqlLimit();
        if (sqlLimit == null) {
            sqlLimit = new SqlLimit();
        }
        Pageable pageable = PageRequest.of(Long.valueOf(sqlLimit.getCurrent() - 1).intValue(), Long.valueOf(sqlLimit.getSize()).intValue());
        org.springframework.data.domain.Page<T> tPage = jpaSpecificationExecutor.findAll(specification, pageable);
        if (tPage == null) {
            return null;
        }
        Page<T> pageVO = new Page<>();
        pageVO.setCurrent(sqlLimit.getCurrent());
        pageVO.setSize(sqlLimit.getSize());
        pageVO.setRecords(tPage.getContent());
        pageVO.setTotal(tPage.getTotalElements());
        return pageVO;

    }


}
