package cn.katoumegumi.java.starter.jdbc.utils;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ws
 */
public class PageConvertUtils {


    public static <T, P> Page<T> copyPage(Page<P> pPage, Class<T> tClass) {
        Page<T> tPage = new Page<>();
        tPage.setCurrent(pPage.getCurrent());
        tPage.setSize(pPage.getSize());
        tPage.setTotal(pPage.getTotal());
        if (WsListUtils.isNotEmpty(pPage.getRecords())) {
            List<P> pList = pPage.getRecords();
            List<T> tList = new ArrayList<>(pList.size());
            for (P p : pList) {
                T t = WsBeanUtils.createObject(tClass);
                assert t != null;
                BeanUtils.copyProperties(p, t);
                tList.add(t);
            }
            tPage.setRecords(tList);
        }
        return tPage;
    }

    public static <T> IPage<T> createEmptyPage(IPage<?> oPage) {
        Page<T> tPage = new Page<>();
        tPage.setCurrent(oPage.getCurrent());
        tPage.setTotal(oPage.getTotal());
        tPage.setSize(oPage.getSize());
        return tPage;
    }


    /*public static <T> Page<T> convertSpringPage(Pageable pageable) {
        Page<T> page = new Page();
        page.setSize(pageable.getPageSize());
        page.setCurrent(page.getCurrent());
        return page;
    }*/


}
