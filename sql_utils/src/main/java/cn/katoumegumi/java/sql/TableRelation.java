package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.SFunction;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.common.TableJoinType;

import java.util.function.Consumer;

/**
 * mySearchList多表关联关系
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
    private Class<?> joinTableClass;

    /**
     * 主表实体路径名称
     */
    private String tableNickName;
    /**
     * 主表实体字段名称
     */
    private String tableColumn;

    /**
     * 关联表实体路径
     */
    private String joinTableNickName;

    /**
     * 关联表实体字段名称
     */
    private String joinTableColumn;

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

    public Class<?> getJoinTableClass() {
        return joinTableClass;
    }

    public TableRelation setJoinTableClass(Class<?> joinTableClass) {
        this.joinTableClass = joinTableClass;
        return this;
    }

    public String getTableNickName() {
        return tableNickName;
    }

    public TableRelation setTableNickName(String tableNickName) {
        this.tableNickName = tableNickName;
        return this;
    }

    public String getTableColumn() {
        return tableColumn;
    }

    public TableRelation setTableColumn(String tableColumn) {
        this.tableColumn = tableColumn;
        return this;
    }


    public String getJoinTableNickName() {
        return joinTableNickName;
    }

    public TableRelation setJoinTableNickName(String joinTableNickName) {
        this.joinTableNickName = joinTableNickName;
        return this;
    }

    public <T> TableRelation setJoinTableNickName(SFunction<T, ?> joinTableNickName) {
        this.joinTableNickName = WsFieldUtils.getFieldName(joinTableNickName);
        return this;
    }

    public <T> TableRelation setJoinTableNickName(String parentTableNickName, SFunction<T, ?> joinTableNickName) {
        if (WsStringUtils.isBlank(parentTableNickName)) {
            this.joinTableNickName = WsFieldUtils.getFieldName(joinTableNickName);
        } else {
            this.joinTableNickName = parentTableNickName + '.' + WsFieldUtils.getFieldName(joinTableNickName);
        }
        return this;
    }

    public String getJoinTableColumn() {
        return joinTableColumn;
    }

    public TableRelation setJoinTableColumn(String joinTableColumn) {
        this.joinTableColumn = joinTableColumn;
        return this;
    }

    public TableRelation on(String tableColumn, String joinTableColumn) {
        this.tableColumn = tableColumn;
        this.joinTableColumn = joinTableColumn;
        return this;
    }


    public <T, R> TableRelation on(SFunction<T, ?> tableColumn, SFunction<R, ?> joinTableColumn) {
        this.tableColumn = WsFieldUtils.getFieldName(tableColumn);
        this.joinTableColumn = WsFieldUtils.getFieldName(joinTableColumn);
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
        MySearchList mySearchList = null;
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
