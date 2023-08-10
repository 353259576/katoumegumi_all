package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsReflectUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.TableJoinType;

import java.util.function.Consumer;

/**
 * mySearchList多表关联关系
 *
 * @author ws
 */
public class TableRelation {

    private MySearchList mySearchList;

    /**
     * 关联类型
     */
    private TableJoinType joinType;

    /**
     * 关联表实体类型
     */
    private Class<?> joinEntityClass;

    /**
     * 主表实体路径名称
     * 可以使用alias
     */
    private String mainEntityPath;
    /**
     * 主表实体字段名称
     */
    private String mainEntityPropertyName;

    /**
     * 关联表实体路径
     */
    private String joinEntityPath;

    /**
     * 关联表实体字段名称
     */
    private String joinEntityPropertyName;

    /**
     * 关联关系额外附加条件
     */
    private MySearchList conditionSearchList;

    /**
     * 关联表实体路径名称别名
     */
    private String alias;


    public TableRelation() {

    }

    public TableRelation(MySearchList mySearchList) {
        this.mySearchList = mySearchList;
    }

    public TableJoinType getJoinType() {
        return joinType;
    }

    public TableRelation setJoinType(TableJoinType joinType) {
        this.joinType = joinType;
        return this;
    }

    public Class<?> getJoinEntityClass() {
        return joinEntityClass;
    }

    public TableRelation setJoinEntityClass(Class<?> joinEntityClass) {
        this.joinEntityClass = joinEntityClass;
        return this;
    }

    public String getTableNickName() {
        return mainEntityPath;
    }

    public TableRelation setTableNickName(String mainEntityPath) {
        this.mainEntityPath = mainEntityPath;
        return this;
    }

    public String getTableColumn() {
        return mainEntityPropertyName;
    }

    public TableRelation setTableColumn(String mainEntityPropertyName) {
        this.mainEntityPropertyName = mainEntityPropertyName;
        return this;
    }


    public String getJoinTableNickName() {
        return joinEntityPath;
    }

    public TableRelation setJoinTableNickName(String joinEntityPath) {
        this.joinEntityPath = joinEntityPath;
        return this;
    }

    public <T> TableRelation setJoinTableNickName(SFunction<T, ?> joinEntityPath) {
        this.joinEntityPath = WsReflectUtils.getFieldName(joinEntityPath);
        return this;
    }

    public <T> TableRelation setJoinTableNickName(String mainEntityPath, SFunction<T, ?> joinEntityPath) {
        if (WsStringUtils.isBlank(mainEntityPath)) {
            this.joinEntityPath = WsReflectUtils.getFieldName(joinEntityPath);
        } else {
            this.joinEntityPath = mainEntityPath + '.' + WsReflectUtils.getFieldName(joinEntityPath);
        }
        return this;
    }

    public String getJoinTableColumn() {
        return joinEntityPropertyName;
    }

    public TableRelation setJoinTableColumn(String joinEntityPropertyName) {
        this.joinEntityPropertyName = joinEntityPropertyName;
        return this;
    }

    public TableRelation on(String mainEntityPropertyName, String joinEntityPropertyName) {
        this.mainEntityPropertyName = mainEntityPropertyName;
        this.joinEntityPropertyName = joinEntityPropertyName;
        return this;
    }


    public <T, R> TableRelation on(SFunction<T, ?> mainEntityPropertyName, SFunction<R, ?> joinEntityPropertyName) {
        this.mainEntityPropertyName = WsReflectUtils.getFieldName(mainEntityPropertyName);
        this.joinEntityPropertyName = WsReflectUtils.getFieldName(joinEntityPropertyName);
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public TableRelation setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public MySearchList end() {
        this.mySearchList.getJoins().add(this);
        return this.mySearchList;
    }

    public MySearchList getConditionSearchList() {
        return conditionSearchList;
    }

    public TableRelation condition(Consumer<MySearchList> searchList) {
        MySearchList mySearchList;
        if (this.conditionSearchList == null) {
            mySearchList = MySearchList.create();
        } else {
            mySearchList = this.conditionSearchList;
        }
        searchList.accept(mySearchList);
        this.conditionSearchList = mySearchList;
        return this;
    }
}
