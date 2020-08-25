package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.hibernate.JpaDataHandle;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author ws
 */
public class PageConvertUtils {


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
