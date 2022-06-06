package cn.katoumegumi.java.sql.handle;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.sql.SQLModelUtils;
import cn.katoumegumi.java.sql.SelectSqlEntity;
import cn.katoumegumi.java.sql.common.SqlCommon;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.entity.ColumnBaseEntity;
import cn.katoumegumi.java.sql.entity.SqlWhereValue;
import cn.katoumegumi.java.sql.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MysqlHandle {

    private static final SqlStringAndParameters EMPTY_SQLSTRING_AND_PARAMETERS = new SqlStringAndParameters(null,null,0,0);

    /**
     * 处理select语句
     * @param selectModel
     * @return
     */
    public static SelectSqlEntity handleSelect(SelectModel selectModel){
        StringBuilder sql = new StringBuilder();
        List<SqlWhereValue> valueList = new ArrayList<>();
        sql.append(SqlCommon.SELECT);
        int columnSize = selectModel.getSelect().size();
        for (int i = 0; i < columnSize - 1; i++){
            ColumnBaseEntity columnBase = selectModel.getSelect().get(i);
            sql.append(SQLModelUtils.guardKeyword(columnBase.getAlias()))
                    .append(SqlCommon.SQL_COMMON_DELIMITER)
                    .append(SQLModelUtils.guardKeyword(columnBase.getColumnName()))
                    .append(SqlCommon.SPACE)
                    .append(SQLModelUtils.guardKeyword(columnBase.getAlias()+SqlCommon.PATH_COMMON_DELIMITER+columnBase.getFieldName()))
                    .append(SqlCommon.COMMA);
        }
        ColumnBaseEntity columnBase = selectModel.getSelect().get(columnSize - 1);
        sql.append(SQLModelUtils.guardKeyword(columnBase.getAlias()))
                .append(SqlCommon.SQL_COMMON_DELIMITER)
                .append(SQLModelUtils.guardKeyword(columnBase.getColumnName()))
                .append(SqlCommon.SPACE)
                .append(SQLModelUtils.guardKeyword(columnBase.getAlias()+SqlCommon.PATH_COMMON_DELIMITER+columnBase.getFieldName()));
        sql.append(SqlCommon.FROM)
                .append(SQLModelUtils.guardKeyword(selectModel.getFrom().getTable().getTableName()))
                .append(SqlCommon.SPACE)
                .append(SQLModelUtils.guardKeyword(selectModel.getFrom().getAlias()));
        if(WsListUtils.isNotEmpty(selectModel.getJoinList())){
            for (JoinTableModel joinTableModel:selectModel.getJoinList()){
                sql.append(joinTableModel.getJoinType().getValue())
                        .append(SQLModelUtils.guardKeyword(joinTableModel.getJoinTable().getTable().getTableName()))
                        .append(SqlCommon.SPACE)
                        .append(SQLModelUtils.guardKeyword(joinTableModel.getJoinTable().getAlias()))
                        .append(SqlCommon.ON);
                handleRelation(joinTableModel.getOn(),sql,valueList,false);
            }
        }
        if(selectModel.getWhere() != null){
            sql.append(SqlCommon.WHERE);
            handleRelation(selectModel.getWhere(),sql,valueList,false);
        }
        if(WsListUtils.isNotEmpty(selectModel.getOrderBy())){
            sql.append(SqlCommon.ORDER_BY);
            int orderByModelSize = selectModel.getOrderBy().size() - 1;
            OrderByModel orderByModel;
            ColumnBaseEntity entity;
            for (int i = 0; i < orderByModelSize; i++){
                orderByModel = selectModel.getOrderBy().get(i);
                entity = orderByModel.getColumn();
                sql.append(SQLModelUtils.guardKeyword(entity.getAlias()))
                        .append(SqlCommon.SQL_COMMON_DELIMITER)
                        .append(SQLModelUtils.guardKeyword(entity.getColumnName()))
                        .append(SqlCommon.SPACE)
                        .append(orderByModel.getType())
                        .append(SqlCommon.COMMA);
            }
            orderByModel = selectModel.getOrderBy().get(orderByModelSize);
            entity = orderByModel.getColumn();
            sql.append(SQLModelUtils.guardKeyword(entity.getAlias()))
                    .append(SqlCommon.SQL_COMMON_DELIMITER)
                    .append(SQLModelUtils.guardKeyword(entity.getColumnName()))
                    .append(SqlCommon.SPACE)
                    .append(orderByModel.getType());

        }
        SelectSqlEntity entity = new SelectSqlEntity();
        entity.setSelectSql(sql.toString());
        entity.setValueList(valueList);
        return entity;
    }


    private static void handleRelation(ConditionRelationModel conditionRelationModel,StringBuilder sql,List<SqlWhereValue> valueList,boolean needBrackets){
        if(WsListUtils.isEmpty(conditionRelationModel.getConditionList())){
            return;
        }
        int conditionSize = conditionRelationModel.getConditionList().size();
        if(conditionSize == 1){
            Condition condition = conditionRelationModel.getConditionList().get(0);
            if(condition instanceof ExpressionCondition){
                handleExpressionCondition((ExpressionCondition) condition,sql,valueList);
            }else if (condition instanceof ConditionRelationModel){
                handleRelation((ConditionRelationModel) condition,sql,valueList,true);
            }
            return;
        }
        if(needBrackets){
            sql.append(SqlCommon.LEFT_BRACKETS);
        }
        Condition condition = conditionRelationModel.getConditionList().get(0);
        if(condition instanceof ExpressionCondition){
            handleExpressionCondition((ExpressionCondition) condition,sql,valueList);
        }else if (condition instanceof ConditionRelationModel){
            handleRelation((ConditionRelationModel) condition,sql,valueList,true);
        }

        for (int i = 1; i < conditionSize; i++){
            sql.append(conditionRelationModel.getRelation().equals(SqlOperator.AND)?SqlCommon.SQL_AND:SqlCommon.SQL_OR);
            condition = conditionRelationModel.getConditionList().get(i);
            if(condition instanceof ExpressionCondition){
                handleExpressionCondition((ExpressionCondition) condition,sql,valueList);
            }else if (condition instanceof ConditionRelationModel){
                handleRelation((ConditionRelationModel) condition,sql,valueList,true);
            }
        }

        if(needBrackets){
            sql.append(SqlCommon.RIGHT_BRACKETS);
        }
    }

    private static void handleExpressionCondition(ExpressionCondition expressionCondition, StringBuilder sql, List<SqlWhereValue> valueList){

        SqlStringAndParameters left;
        SqlStringAndParameters right;

        switch (expressionCondition.getSymbol()){
            case SQL:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                if(left.getSql() != null) {
                    sql.append(left.getSql());
                }
                sqlStringAndParametersValueAddInList(left,valueList);
                break;
            case IN:
            case NOT_IN:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                right = handleExpressionConditionValue(expressionCondition.getRightType(),expressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(expressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommon.LEFT_BRACKETS);
                if(right.isOnlyValue()) {
                    sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommon.PLACEHOLDER, SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(right, valueList);
                }else {
                    sql.append(right.getSql());
                    sqlStringAndParametersValueAddInList(right,valueList);
                }
                sql.append(SqlCommon.RIGHT_BRACKETS);
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                right = handleExpressionConditionValue(expressionCondition.getRightType(),expressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(expressionCondition.getSymbol().getSymbol());
                sql.append(createPlaceholder(right.getPlaceholderNum(),SqlCommon.PLACEHOLDER,SqlCommon.SQL_AND));
                sqlStringAndParametersValueAddInList(right,valueList);
                break;
            case EXISTS:
            case NOT_EXISTS:
                sql.append(expressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommon.LEFT_BRACKETS);
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                sql.append(left.getSql());
                sqlStringAndParametersValueAddInList(left,valueList);
                sql.append(SqlCommon.RIGHT_BRACKETS);
                break;
            case NULL:
            case NOT_NULL:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(expressionCondition.getSymbol().getSymbol());
                break;
            case LIKE:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(expressionCondition.getSymbol().getSymbol());
                right = handleExpressionConditionValue(expressionCondition.getRightType(),expressionCondition.getRight());
                String searchKey = WsBeanUtils.convertBean(right.getValue(),String.class);
                if(searchKey == null){
                    throw new IllegalArgumentException("模糊查询条件不能为空");
                }
                int checkKey = 0;
                if(searchKey.startsWith("%")){
                    checkKey |= 1;
                }
                if(searchKey.endsWith("%")){
                    checkKey |= 2;
                }
                switch (checkKey){
                    case 0:
                        sql.append("concat('%',?,'%')");
                        break;
                    case 1:
                        searchKey = searchKey.substring(1);
                        sql.append("concat('%',?)");
                        break;
                    case 2:
                        sql.append("concat(?,'%')");
                        searchKey = searchKey.substring(0,searchKey.length() - 1);
                        break;
                    case 3:
                        sql.append("concat('%',?,'%')");
                        searchKey = searchKey.substring(1,searchKey.length() - 1);
                        break;
                }
                valueList.add(new SqlWhereValue(null,searchKey));
                break;
            default:
                left = handleExpressionConditionValue(expressionCondition.getLeftType(),expressionCondition.getLeft());
                right = handleExpressionConditionValue(expressionCondition.getRightType(),expressionCondition.getRight());
                if(left.isOnlyValue()){
                    sql.append(createPlaceholder(left.getPlaceholderNum(),SqlCommon.PLACEHOLDER,SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(left,valueList);
                }else {
                    sql.append(left.getSql());
                }
                sql.append(expressionCondition.getSymbol().getSymbol());
                if (right.isOnlyValue()){
                    sql.append(createPlaceholder(right.getPlaceholderNum(),SqlCommon.PLACEHOLDER,SqlCommon.COMMA));
                    sqlStringAndParametersValueAddInList(right,valueList);
                }else {
                    sql.append(right.getSql());
                }
                break;
        }

    }

    private static SqlStringAndParameters handleExpressionConditionValue(int type,Object value){
        switch (type){
            case 0:
                return EMPTY_SQLSTRING_AND_PARAMETERS;
            case 1:
                return new SqlStringAndParameters(null,value,1,1);
            case 2:
                return new SqlStringAndParameters(null,value,((Collection<?>)value).size(),2);
            case 3:
                return new SqlStringAndParameters(null,value,((Object[])value).length,3);
            case 4:
                SelectSqlEntity entity = MysqlHandle.handleSelect((SelectModel) value);
                return new SqlStringAndParameters(entity.getSelectSql(),entity.getValueList(),entity.getValueList().size());
            case 5:
                //暂时没处理
                return EMPTY_SQLSTRING_AND_PARAMETERS;
            case 6:
                ColumnBaseEntity columnBaseEntity = (ColumnBaseEntity) value;
                return new SqlStringAndParameters(
                        SQLModelUtils.guardKeyword(columnBaseEntity.getAlias()) + SqlCommon.SQL_COMMON_DELIMITER + SQLModelUtils.guardKeyword(columnBaseEntity.getColumnName()),
                        null,
                        0,
                        0
                );
            case 7:
                SqlStringModel sqlStringModel = (SqlStringModel) value;
                return new SqlStringAndParameters(sqlStringModel.getSql(),sqlStringModel.getValue());
            case 8:
                StringBuilder stringBuilder = new StringBuilder();
                List<SqlWhereValue> objectList = new ArrayList<>();
                handleRelation((ConditionRelationModel) value,stringBuilder,objectList,true);
                return new SqlStringAndParameters(stringBuilder.toString(),objectList,objectList.size(),2);
            default:throw new IllegalArgumentException("不支持的类:" + value.getClass());
        }
    }



    /**
     * 将value加入列表
     * @param valueList
     */
    private static void sqlStringAndParametersValueAddInList(SqlStringAndParameters sqlStringAndParameters,List<SqlWhereValue> valueList){
        switch (sqlStringAndParameters.getValueType()){
            case 1:
                valueList.add(new SqlWhereValue(null,sqlStringAndParameters.getValue()));
                break;
            case 2:
                Collection<?> collection = (Collection<?>) sqlStringAndParameters.getValue();
                if(WsListUtils.isNotEmpty(collection)) {
                    for (Object o:collection){
                        valueList.add(new SqlWhereValue(null,o));
                    }
                }
                break;
            case 3:
                Object[] objects = (Object[]) sqlStringAndParameters.getValue();
                if(WsListUtils.isNotEmpty(objects)) {
                    for (Object o:objects){
                        valueList.add(new SqlWhereValue(null,o));
                    }
                }
                break;
            default:break;
        }
    }

    /**
     * 生成由分隔符分割的任意数量的占位符字符串
     * @param placeholderSize 占位符数量
     * @param placeholderStr 占位符
     * @param separator 分隔符
     * @return
     */
    private static String createPlaceholder(int placeholderSize,String placeholderStr,String separator){
        if(placeholderSize == 0){
            return "";
        }
        if(placeholderSize == 1){
            return placeholderStr;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < placeholderSize - 1; i++){
            stringBuilder.append(placeholderStr)
                    .append(separator);
        }
        stringBuilder.append(placeholderStr);
        return stringBuilder.toString();
    }



}
