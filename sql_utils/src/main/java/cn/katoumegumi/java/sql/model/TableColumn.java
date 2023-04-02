package cn.katoumegumi.java.sql.model;

import cn.katoumegumi.java.sql.FieldColumnRelation;

import java.lang.reflect.Field;

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
    Field getField();

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
    String getFieldName();

    FieldColumnRelation getFieldColumnRelation();

}
