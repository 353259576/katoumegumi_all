package cn.katoumegumi.java.sql.model.component;

import cn.katoumegumi.java.common.model.BeanPropertyModel;
import cn.katoumegumi.java.sql.mapper.model.PropertyBaseColumnRelation;

public interface TableColumn {

    /**
     * 是否是id
     *
     * @return
     */
    boolean isId();

    /**
     * 对应bean field
     *
     * @return
     */
    BeanPropertyModel getBeanProperty();

    /**
     * 表的相对路径
     *
     * @return
     */
    String getTablePath();

    /**
     * 表别名
     *
     * @return
     */
    String getTableAlias();

    /**
     * column对应的bean的field的名称
     *
     * @return
     */
    String getBeanPropertyName();

    /**
     * 获取列别名
     * @return
     */
    String getColumnAlias();

    PropertyBaseColumnRelation getFieldColumnRelation();

}
