package cn.katoumegumi.java.utils;

import cn.katoumegumi.java.hibernate.JpaDataHandle;
import cn.katoumegumi.java.sql.MySearchList;
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
        Page<T> pageVO = mySearchList.getPageVO();
        if (pageVO == null) {
            pageVO = new Page<T>();
        }
        Pageable pageable = PageRequest.of(Long.valueOf(pageVO.getCurrent() - 1).intValue(), Long.valueOf(pageVO.getSize()).intValue());
        org.springframework.data.domain.Page<T> tPage = jpaSpecificationExecutor.findAll(specification, pageable);
        if (tPage == null) {
            return null;
        }
        pageVO.setRecords(tPage.getContent());
        pageVO.setTotal(tPage.getTotalElements());
        return pageVO;

    }


}
