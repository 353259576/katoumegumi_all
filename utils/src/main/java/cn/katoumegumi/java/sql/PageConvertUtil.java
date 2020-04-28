package cn.katoumegumi.java.sql;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author 10480
 */
public class PageConvertUtil {


    public static  <T, P> IPage<T> convertPage(IPage<P> page) {
        Page<T> tPage = new Page<>();
        tPage.setCurrent(page.getCurrent());
        tPage.setSize(page.getSize());
        tPage.setTotal(page.getTotal());
        return tPage;
    }


}
