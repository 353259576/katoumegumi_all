package cn.katoumegumi.java.sql.common;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.*;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.ColumnConditionEntity;

import java.util.*;

/**
 * @author 10480
 */

public enum SqlOperator {

    /**
     * 等于
     */
    EQ((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = ?";
    }),
    EQP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * 模糊查询
     */
    LIKE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        String fuzzyWord = WsBeanUtils.objectToT(mySearch.getValue(), String.class);
        assert fuzzyWord != null;
        int start = 0;
        int end = fuzzyWord.length();
        if (fuzzyWord.charAt(0) == '%') {
            start = 1;
        }
        if (fuzzyWord.charAt(fuzzyWord.length() - 1) == '%') {
            end = end - 1;
        }
        baseWhereValueList.add(fuzzyWord.substring(start, end));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " like concat('%',?,'%')";
    }),
    /**
     * 大于
     */
    GT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " > ?";
    }),
    GTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " > " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * 小于
     */
    LT((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " < ?";
    }),
    LTP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " < " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * 大于等于
     */
    GTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " >= ?";
    }),
    GTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " >= " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * 小于等于
     */
    LTE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " <= ?";
    }),
    LTEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " <= " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * in
     */
    IN((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(" in(");
            tableColumn.append(entity.getSelectSql());
            tableColumn.append(")");
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                Iterator<?> iterator = collection.iterator();
                List<String> symbols = new ArrayList<>();
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    symbols.add("?");
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
                }
                tableColumn.append(" in");
                tableColumn.append('(');
                tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                tableColumn.append(')');

            } else if (mySearch.getValue().getClass().isArray()) {
                Object[] os = (Object[]) mySearch.getValue();
                List<String> symbols = new ArrayList<>();
                for (Object o : os) {
                    symbols.add("?");
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
                }
                tableColumn.append(" in");
                tableColumn.append('(');
                tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                tableColumn.append(')');
            } else {
                throw new RuntimeException(columnBaseEntity.getFieldName() + "参数非数组类型");
            }
        }
        return tableColumn.toString();
    }),
    /**
     * not in
     */
    NIN((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(" not in(");
            tableColumn.append(entity.getSelectSql());
            tableColumn.append(")");
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            if (WsFieldUtils.classCompare(mySearch.getValue().getClass(), Collection.class)) {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                Iterator<?> iterator = collection.iterator();
                List<String> symbols = new ArrayList<>();
                while (iterator.hasNext()) {
                    Object o = iterator.next();
                    symbols.add("?");
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
                }
                tableColumn.append(" not in");
                tableColumn.append('(');
                tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                tableColumn.append(')');

            } else if (mySearch.getValue().getClass().isArray()) {
                Object[] os = (Object[]) mySearch.getValue();
                List<String> symbols = new ArrayList<>();
                for (Object o : os) {
                    symbols.add("?");
                    baseWhereValueList.add(WsBeanUtils.objectToT(o, columnBaseEntity.getFieldColumnRelation().getFieldClass()));
                }
                tableColumn.append(" not in");
                tableColumn.append('(');
                tableColumn.append(WsStringUtils.jointListString(symbols, ","));
                tableColumn.append(')');
            } else {
                throw new RuntimeException(columnBaseEntity.getFieldName() + "参数非数组类型");
            }
        }
        return tableColumn.toString();
    }),
    /**
     * not null
     */
    NOTNULL((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " is not null";
    }),
    /**
     * is null
     */
    NULL((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " is null";
    }),
    /**
     * 不等于
     */
    NE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " != ?";
    }),
    NEP(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        ColumnBaseEntity conditionColumn = translateNameUtils.getColumnBaseEntity(WsStringUtils.anyToString(mySearch.getValue()), prefix);
        return SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " != " +
                SQLModelUtils.guardKeyword(conditionColumn.getAlias()) +
                '.' +
                SQLModelUtils.guardKeyword(conditionColumn.getColumnName());
    })),
    /**
     * 嵌入sql 只有hibernate支持
     */
    SQL(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        if (mySearch.getValue() != null) {
            if (mySearch.getValue() instanceof Collection) {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                baseWhereValueList.addAll(collection);
            } else if (mySearch.getValue().getClass().isArray()) {
                Object[] os = (Object[]) mySearch.getValue();
                baseWhereValueList.addAll(Arrays.asList(os));
            } else {
                baseWhereValueList.add(mySearch.getValue());
            }
        }
        return translateNameUtils.translateTableNickName(prefix, mySearch.getFieldName());
    })),
    EXISTS(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(" exists (");
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(entity.getSelectSql());
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            tableColumn.append(translateNameUtils.translateTableNickName(prefix, mySearch.getFieldName()));
            if (mySearch.getValue() != null) {
                if (mySearch.getValue() instanceof Collection) {
                    Collection<?> collection = (Collection<?>) mySearch.getValue();
                    baseWhereValueList.addAll(collection);
                } else if (mySearch.getValue().getClass().isArray()) {
                    Object[] os = (Object[]) mySearch.getValue();
                    baseWhereValueList.addAll(Arrays.asList(os));
                } else {
                    baseWhereValueList.add(mySearch.getValue());
                }
            }
        }
        tableColumn.append(") ");
        return tableColumn.toString();
    })),
    NOT_EXISTS(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(" not exists (");
        if (mySearch.getValue() instanceof MySearchList) {
            SQLModelUtils sqlModelUtils = new SQLModelUtils((MySearchList) mySearch.getValue(), translateNameUtils);
            SelectSqlEntity entity = sqlModelUtils.select();
            tableColumn.append(entity.getSelectSql());
            if (WsListUtils.isNotEmpty(entity.getValueList())) {
                baseWhereValueList.addAll(entity.getValueList());
            }
        } else {
            tableColumn.append(translateNameUtils.translateTableNickName(prefix, mySearch.getFieldName()));
            if (mySearch.getValue() != null) {
                if (mySearch.getValue() instanceof Collection) {
                    Collection<?> collection = (Collection<?>) mySearch.getValue();
                    baseWhereValueList.addAll(collection);
                } else if (mySearch.getValue().getClass().isArray()) {
                    Object[] os = (Object[]) mySearch.getValue();
                    baseWhereValueList.addAll(Arrays.asList(os));
                } else {
                    baseWhereValueList.add(mySearch.getValue());
                }
            }
        }
        tableColumn.append(") ");
        return tableColumn.toString();
    })),
    BETWEEN(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (WsBeanUtils.isArray(mySearch.getValue().getClass())) {
            tableColumn.append(" between ");
            if (mySearch.getValue().getClass().isArray()) {
                Object[] objects = (Object[]) mySearch.getValue();
                if (objects.length != 2) {

                    throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                }
                tableColumn
                        .append(WsBeanUtils.objectToT(objects[0], columnBaseEntity.getField().getType()))
                        .append(" AND ")
                        .append(WsBeanUtils.objectToT(objects[1], columnBaseEntity.getField().getType()));
                baseWhereValueList.add(objects[0]);
                baseWhereValueList.add(objects[1]);
            } else {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                if (collection.size() != 2) {
                    throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                }
                Iterator<?> iterator = collection.iterator();
                tableColumn
                        .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()))
                        .append(" AND ")
                        .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()));
                baseWhereValueList.addAll(collection);
            }
        }
        return tableColumn.toString();
    })),
    NOT_BETWEEN(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        StringBuilder tableColumn = new StringBuilder();
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
        tableColumn.append(".");
        tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        if (WsBeanUtils.isArray(mySearch.getValue().getClass())) {
            tableColumn.append(" not between ");
            if (mySearch.getValue().getClass().isArray()) {
                Object[] objects = (Object[]) mySearch.getValue();
                if (objects.length != 2) {
                    throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                }
                tableColumn
                        .append(WsBeanUtils.objectToT(objects[0], columnBaseEntity.getField().getType()))
                        .append(" AND ")
                        .append(WsBeanUtils.objectToT(objects[1], columnBaseEntity.getField().getType()));
                baseWhereValueList.add(objects[0]);
                baseWhereValueList.add(objects[1]);
            } else {
                Collection<?> collection = (Collection<?>) mySearch.getValue();
                if (collection.size() != 2) {
                    throw new RuntimeException(columnBaseEntity.getFieldName() + "between只能允许有两个值");
                }
                Iterator<?> iterator = collection.iterator();
                tableColumn
                        .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()))
                        .append(" AND ")
                        .append(WsBeanUtils.objectToT(iterator.next(), columnBaseEntity.getField().getType()));
                baseWhereValueList.addAll(collection);
            }
        }
        return tableColumn.toString();
    })),
    /**
     * 排序
     */
    SORT(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        StringBuilder tableColumn = new StringBuilder();
        if(mySearch.getFieldName().endsWith(")")){
            tableColumn.append(mySearch.getFieldName());
        }else {
            ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
            tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()));
            tableColumn.append(".");
            tableColumn.append(SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()));
        }
        tableColumn.append(' ');
        tableColumn.append(mySearch.getValue());
        return tableColumn.toString();

    })),
    /**
     * and
     */
    AND(null),
    /**
     * or
     */
    OR(null),
    /**
     * 修改
     */
    SET(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = ? ";
    })),
    /**
     * 加
     */
    ADD(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = IFNULL(" + SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) + ",0) + ? ";

    })),
    /**
     * 减
     */
    SUBTRACT(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = IFNULL(" + SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) + ",0) - ? ";
    })),
    /**
     * 乘
     */
    MULTIPLY(((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = IFNULL(" + SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) + ",0) * ? ";
    })),
    /**
     * 除
     */
    DIVIDE((translateNameUtils, mySearch, prefix, baseWhereValueList) -> {
        ColumnBaseEntity columnBaseEntity = translateNameUtils.getColumnBaseEntity(mySearch.getFieldName(), prefix);
        baseWhereValueList.add(WsBeanUtils.objectToT(mySearch.getValue(),columnBaseEntity.getFieldColumnRelation().getFieldClass()));
        return SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                '.' +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) +
                " = IFNULL(" + SQLModelUtils.guardKeyword(translateNameUtils.getAbbreviation(prefix)) +
                "." +
                SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()) + ",0) / ? ";
    });

    //private final String value;

    private final ColumnConditionHandle handle;

    private SqlOperator(ColumnConditionHandle handle) {
        //this.value = value;
        this.handle = handle;
    }


    public ColumnConditionHandle getHandle() {
        return handle;
    }

    public interface ColumnConditionHandle{

        public String handle(TranslateNameUtils translateNameUtils, MySearch mySearch, String prefix, List<Object> baseWhereValueList);
    }

}
