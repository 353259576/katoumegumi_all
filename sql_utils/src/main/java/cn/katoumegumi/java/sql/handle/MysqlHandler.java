package cn.katoumegumi.java.sql.handle;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsCollectionUtils;
import cn.katoumegumi.java.sql.SQLModelFactory;
import cn.katoumegumi.java.sql.common.SqlCommonConstants;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.common.ValueTypeConstants;
import cn.katoumegumi.java.sql.handle.model.DeleteSqlEntity;
import cn.katoumegumi.java.sql.handle.model.SelectSqlEntity;
import cn.katoumegumi.java.sql.handle.model.SqlParameter;
import cn.katoumegumi.java.sql.handle.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.model.component.*;
import cn.katoumegumi.java.sql.model.condition.*;
import cn.katoumegumi.java.sql.model.result.DeleteModel;
import cn.katoumegumi.java.sql.model.result.SelectModel;
import cn.katoumegumi.java.sql.model.result.UpdateModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MysqlHandler {



    /**
     * 处理select语句
     * @param selectModel
     * @return
     */
    public static SelectSqlEntity handleSelect(SelectModel selectModel) {


        List<SqlParameter> valueList = new ArrayList<>();

        //处理column
        StringBuilder columnSql = new StringBuilder();
        int columnSize = selectModel.getSelect().size();
        for (int i = 0; i < columnSize - 1; i++) {
            TableColumn tableColumn = selectModel.getSelect().get(i);
            if (tableColumn instanceof BaseTableColumn) {
                BaseTableColumn columnBase = (BaseTableColumn) tableColumn;
                columnSql.append(SQLModelFactory.guardKeyword(columnBase.getTableAlias()))
                        .append(SqlCommonConstants.SQL_COMMON_DELIMITER)
                        .append(SQLModelFactory.guardKeyword(columnBase.getColumnName()));
            } else if (tableColumn instanceof DynamicTableColumn) {
                DynamicTableColumn dynamicTableColumn = (DynamicTableColumn) tableColumn;
                chooseHandleConditionFunction(dynamicTableColumn.getDynamicColumn(), columnSql, valueList);
            }
            columnSql.append(SqlCommonConstants.SPACE)
                    .append(SQLModelFactory.guardKeyword(tableColumn.getTableAlias() + SqlCommonConstants.PATH_COMMON_DELIMITER + tableColumn.getBeanPropertyName()))
                    .append(SqlCommonConstants.COMMA);
        }
        TableColumn tableColumn = selectModel.getSelect().get(columnSize - 1);
        if (tableColumn instanceof BaseTableColumn) {
            BaseTableColumn columnBase = (BaseTableColumn) tableColumn;
            columnSql.append(SQLModelFactory.guardKeyword(columnBase.getTableAlias()))
                    .append(SqlCommonConstants.SQL_COMMON_DELIMITER)
                    .append(SQLModelFactory.guardKeyword(columnBase.getColumnName()));
        } else if (tableColumn instanceof DynamicTableColumn) {
            DynamicTableColumn dynamicTableColumn = (DynamicTableColumn) tableColumn;
            chooseHandleConditionFunction(dynamicTableColumn.getDynamicColumn(), columnSql, valueList);
        }
        columnSql.append(SqlCommonConstants.SPACE)
                .append(SQLModelFactory.guardKeyword(tableColumn.getTableAlias() + SqlCommonConstants.PATH_COMMON_DELIMITER + tableColumn.getBeanPropertyName()));
        //处理关联表
        StringBuilder tableAndJoinTableSql = new StringBuilder();
        tableAndJoinTableSql.append(SqlCommonConstants.FROM)
                .append(SQLModelFactory.guardKeyword(selectModel.getFrom().getTable().getTableName()))
                .append(SqlCommonConstants.SPACE)
                .append(SQLModelFactory.guardKeyword(selectModel.getFrom().getAlias()));
        if (WsCollectionUtils.isNotEmpty(selectModel.getJoinList())) {
            for (JoinTableModel joinTableModel : selectModel.getJoinList()) {
                tableAndJoinTableSql.append(joinTableModel.getJoinType().getValue())
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getTable().getTableName()))
                        .append(SqlCommonConstants.SPACE)
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getAlias()))
                        .append(SqlCommonConstants.ON);
                handleRelation(joinTableModel.getOn(), tableAndJoinTableSql, valueList, false);
            }
        }

        //处理查询条件
        StringBuilder whereConditionSql = new StringBuilder();
        if (selectModel.getWhere() != null) {
            whereConditionSql.append(SqlCommonConstants.WHERE);
            handleRelation(selectModel.getWhere(), whereConditionSql, valueList, false);
        }

        //处理order by条件
        StringBuilder orderBySql = new StringBuilder();
        if (WsCollectionUtils.isNotEmpty(selectModel.getOrderBy())) {
            orderBySql.append(SqlCommonConstants.ORDER_BY);
            int orderByModelSize = selectModel.getOrderBy().size() - 1;
            OrderByCondition orderByCondition;
            BaseTableColumn entity;
            for (int i = 0; i < orderByModelSize; i++) {
                orderByCondition = selectModel.getOrderBy().get(i);
                entity = orderByCondition.getColumn();
                if (entity == null) {
                    orderBySql.append(orderByCondition.getSql().getSql());
                } else {
                    orderBySql.append(SQLModelFactory.guardKeyword(entity.getTableAlias()))
                            .append(SqlCommonConstants.SQL_COMMON_DELIMITER)
                            .append(SQLModelFactory.guardKeyword(entity.getColumnName()));
                }
                orderBySql.append(SqlCommonConstants.SPACE)
                        .append(orderByCondition.getType())
                        .append(SqlCommonConstants.COMMA);

            }
            orderByCondition = selectModel.getOrderBy().get(orderByModelSize);
            entity = orderByCondition.getColumn();
            if (entity == null) {
                orderBySql.append(orderByCondition.getSql().getSql());
            } else {
                orderBySql.append(SQLModelFactory.guardKeyword(entity.getTableAlias()))
                        .append(SqlCommonConstants.SQL_COMMON_DELIMITER)
                        .append(SQLModelFactory.guardKeyword(entity.getColumnName()));
            }
            orderBySql.append(SqlCommonConstants.SPACE)
                    .append(orderByCondition.getType());
        }

        //处理limit语句
        StringBuilder limitSql = new StringBuilder();
        if (selectModel.getSqlLimit() != null) {
            limitSql.append(" limit ")
                    .append(selectModel.getSqlLimit().getOffset())
                    .append(",")
                    .append(selectModel.getSqlLimit().getSize());
        }

        String sql = SqlCommonConstants.SELECT +
                columnSql +
                tableAndJoinTableSql +
                whereConditionSql +
                orderBySql +
                limitSql;

        String countSql = SqlCommonConstants.SELECT +
                "count(*) " +
                tableAndJoinTableSql +
                whereConditionSql;

        SelectSqlEntity entity = new SelectSqlEntity();
        entity.setSelectSql(sql);
        entity.setCountSql(countSql);
        entity.setValueList(valueList);
        return entity;
    }


    public static DeleteSqlEntity handleDelete(DeleteModel deleteModel) {
        List<SqlParameter> valueList = new ArrayList<>();

        String mainTableAlias = SQLModelFactory.guardKeyword(deleteModel.getFrom().getAlias());

        //处理关联表
        StringBuilder tableAndJoinTableSql = new StringBuilder();
        tableAndJoinTableSql.append(SqlCommonConstants.FROM)
                .append(SQLModelFactory.guardKeyword(deleteModel.getFrom().getTable().getTableName()))
                .append(SqlCommonConstants.SPACE)
                .append(mainTableAlias);
        if (WsCollectionUtils.isNotEmpty(deleteModel.getJoinList())) {
            for (JoinTableModel joinTableModel : deleteModel.getJoinList()) {
                tableAndJoinTableSql.append(joinTableModel.getJoinType().getValue())
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getTable().getTableName()))
                        .append(SqlCommonConstants.SPACE)
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getAlias()))
                        .append(SqlCommonConstants.ON);
                handleRelation(joinTableModel.getOn(), tableAndJoinTableSql, valueList, false);
            }
        }

        //处理查询条件
        StringBuilder whereConditionSql = new StringBuilder();
        if (deleteModel.getWhere() != null) {
            whereConditionSql.append(SqlCommonConstants.WHERE);
            handleRelation(deleteModel.getWhere(), whereConditionSql, valueList, false);
        }

        String sql = SqlCommonConstants.DELETE +
                mainTableAlias +
                tableAndJoinTableSql +
                whereConditionSql;
        return new DeleteSqlEntity(sql, valueList);
    }

    public static UpdateSqlEntity handleUpdate(UpdateModel updateModel) {
        List<SqlParameter> valueList = new ArrayList<>();
        //处理关联表
        StringBuilder tableAndJoinTableSql = new StringBuilder();
        tableAndJoinTableSql
                .append(SQLModelFactory.guardKeyword(updateModel.getFrom().getTable().getTableName()))
                .append(SqlCommonConstants.SPACE)
                .append(SQLModelFactory.guardKeyword(updateModel.getFrom().getAlias()));
        if (WsCollectionUtils.isNotEmpty(updateModel.getJoinList())) {
            for (JoinTableModel joinTableModel : updateModel.getJoinList()) {
                tableAndJoinTableSql.append(joinTableModel.getJoinType().getValue())
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getTable().getTableName()))
                        .append(SqlCommonConstants.SPACE)
                        .append(SQLModelFactory.guardKeyword(joinTableModel.getJoinTable().getAlias()))
                        .append(SqlCommonConstants.ON);
                handleRelation(joinTableModel.getOn(), tableAndJoinTableSql, valueList, false);
            }
        }

        StringBuilder updateSetSql = new StringBuilder();

        chooseHandleConditionFunction(updateModel.getUpdateConditionList().get(0), updateSetSql, valueList);
        for (int i = 1; i < updateModel.getUpdateConditionList().size(); i++) {
            updateSetSql.append(SqlCommonConstants.SQL_COMMON_UPDATE_SET_CONDITION_DELIMITER);
            chooseHandleConditionFunction(updateModel.getUpdateConditionList().get(i), updateSetSql, valueList);
        }

        //处理查询条件
        StringBuilder whereConditionSql = new StringBuilder();
        if (updateModel.getWhere() != null) {
            whereConditionSql.append(SqlCommonConstants.WHERE);
            handleRelation(updateModel.getWhere(), whereConditionSql, valueList, false);
        }

        String updateSql = SqlCommonConstants.UPDATE
                + SqlCommonConstants.SPACE
                + tableAndJoinTableSql
                + SqlCommonConstants.SET
                + updateSetSql
                + whereConditionSql;

        UpdateSqlEntity updateSqlEntity = new UpdateSqlEntity();
        updateSqlEntity.setUpdateSql(updateSql);
        updateSqlEntity.setValueList(valueList);
        return updateSqlEntity;
    }


    private static void handleRelation(RelationCondition relationCondition, StringBuilder sql, List<SqlParameter> valueList, boolean needBrackets) {
        if (WsCollectionUtils.isEmpty(relationCondition.getConditionList())) {
            return;
        }
        int conditionSize = relationCondition.getConditionList().size();
        if (conditionSize == 1) {
            Condition condition = relationCondition.getConditionList().get(0);
            chooseHandleConditionFunction(condition, sql, valueList);
            return;
        }
        if (needBrackets) {
            sql.append(SqlCommonConstants.LEFT_BRACKETS);
        }
        Condition condition = relationCondition.getConditionList().get(0);
        chooseHandleConditionFunction(condition, sql, valueList);

        for (int i = 1; i < conditionSize; i++) {
            sql.append(relationCondition.getRelation().equals(SqlOperator.AND) ? SqlCommonConstants.SQL_AND : SqlCommonConstants.SQL_OR);
            condition = relationCondition.getConditionList().get(i);
            chooseHandleConditionFunction(condition, sql, valueList);
        }

        if (needBrackets) {
            sql.append(SqlCommonConstants.RIGHT_BRACKETS);
        }
    }

    private static void chooseHandleConditionFunction(Condition condition, StringBuilder sql, List<SqlParameter> valueList) {
        if (condition instanceof SingleExpressionCondition) {
            handleSingleExpressionCondition((SingleExpressionCondition) condition, sql, valueList);
        } else if (condition instanceof RelationCondition) {
            handleRelation((RelationCondition) condition, sql, valueList, true);
        } else if (condition instanceof MultiExpressionCondition) {
            handleMultiExpressionCondition((MultiExpressionCondition) condition, sql, valueList);
        } else if (condition instanceof SqlFunctionCondition) {
            handleSqlFunctionCondition((SqlFunctionCondition) condition, sql, valueList);
        }
    }

    private static void handleSingleExpressionCondition(SingleExpressionCondition singleExpressionCondition, StringBuilder sql, List<SqlParameter> valueList) {

        SqlStringAndParameters left;
        SqlStringAndParameters right;

        switch (singleExpressionCondition.getSymbol()) {
            case SQL:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                if (left.getSql() != null) {
                    sql.append(left.getSql());
                }
                sqlStringAndParametersValueAddInList(left, valueList);
                break;
            case IN:
            case NOT_IN:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommonConstants.LEFT_BRACKETS);
                if (right.isOnlyValue()) {
                    sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
                    sqlStringAndParametersValueAddInList(right, valueList);
                } else {
                    sql.append(right.getSql());
                    sqlStringAndParametersValueAddInList(right, valueList);
                }
                sql.append(SqlCommonConstants.RIGHT_BRACKETS);
                break;
            case BETWEEN:
            case NOT_BETWEEN:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.SQL_AND));
                sqlStringAndParametersValueAddInList(right, valueList);
                break;
            case EXISTS:
            case NOT_EXISTS:
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                sql.append(SqlCommonConstants.LEFT_BRACKETS);
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sqlStringAndParametersValueAddInList(left, valueList);
                sql.append(SqlCommonConstants.RIGHT_BRACKETS);
                break;
            case NULL:
            case NOT_NULL:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                break;
            case LIKE:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                sql.append(left.getSql());
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                String searchKey = WsBeanUtils.convertBean(right.getValue(), String.class);
                if (searchKey == null) {
                    throw new IllegalArgumentException("模糊查询条件不能为空");
                }
                int checkKey = 0;
                if (searchKey.startsWith("%")) {
                    checkKey |= 1;
                }
                if (searchKey.endsWith("%")) {
                    checkKey |= 2;
                }
                switch (checkKey) {
                    case 0:
                        sql.append("concat('%',?,'%')");
                        break;
                    case 1:
                        searchKey = searchKey.substring(1);
                        sql.append("concat('%',?)");
                        break;
                    case 2:
                        sql.append("concat(?,'%')");
                        searchKey = searchKey.substring(0, searchKey.length() - 1);
                        break;
                    case 3:
                        sql.append("concat('%',?,'%')");
                        searchKey = searchKey.substring(1, searchKey.length() - 1);
                        break;
                }
                valueList.add(new SqlParameter(null, searchKey));
                break;
            default:
                left = handleExpressionConditionValue(singleExpressionCondition.getLeftType(), singleExpressionCondition.getLeft());
                right = handleExpressionConditionValue(singleExpressionCondition.getRightType(), singleExpressionCondition.getRight());
                if (left.isOnlyValue()) {
                    sql.append(createPlaceholder(left.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
                    sqlStringAndParametersValueAddInList(left, valueList);
                } else {
                    sql.append(left.getSql());
                }
                sql.append(singleExpressionCondition.getSymbol().getSymbol());
                if (right.isOnlyValue()) {
                    sql.append(createPlaceholder(right.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
                    sqlStringAndParametersValueAddInList(right, valueList);
                } else {
                    sql.append(right.getSql());
                }
                break;
        }

    }

    private static void handleMultiExpressionCondition(MultiExpressionCondition multiExpressionCondition, StringBuilder sql, List<SqlParameter> valueList) {

        for (int i = 0; i < multiExpressionCondition.getLength(); i++) {
            int type = multiExpressionCondition.getTypes()[i];
            Object value = multiExpressionCondition.getValues()[i];
            SqlStringAndParameters sqlStringAndParameters = handleExpressionConditionValue(type, value);

            boolean addBrackets = true;
            switch (type) {
                case ValueTypeConstants.NULL_TYPE:
                    //continue;
                case ValueTypeConstants.COLUMN_NAME_TYPE:
                case ValueTypeConstants.SYMBOL_TYPE:
                case ValueTypeConstants.BASE_VALUE_TYPE:
                case ValueTypeConstants.SQL_FUNCTION_CONDITION:
                case ValueTypeConstants.NULL_VALUE_MODEL:
                    addBrackets = false;
                    break;
            }
            /*if (i != 0) {
                sql.append(SqlCommon.SPACE);
            }*/
            if (addBrackets) {
                sql.append(SqlCommonConstants.LEFT_BRACKETS);
            }
            if (sqlStringAndParameters.isOnlyValue()) {
                sql.append(createPlaceholder(sqlStringAndParameters.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
            } else {
                sql.append(sqlStringAndParameters.getSql());
            }
            sqlStringAndParametersValueAddInList(sqlStringAndParameters, valueList);
            if (addBrackets) {
                sql.append(SqlCommonConstants.RIGHT_BRACKETS);
            }

        }

    }

    private static SqlStringAndParameters handleExpressionConditionValue(int type, Object value) {
        switch (type) {
            case ValueTypeConstants.NULL_TYPE:
                return SqlCommonConstants.EMPTY_SQL_STRING_AND_PARAMETERS;
            case ValueTypeConstants.NULL_VALUE_MODEL:
                return SqlCommonConstants.NULL_VALUE_SQL_STRING_AND_PARAMETERS;
            case ValueTypeConstants.BASE_VALUE_TYPE:
                return new SqlStringAndParameters(null, value, 1, ValueTypeConstants.BASE_VALUE_TYPE);
            case ValueTypeConstants.COLLECTION_TYPE:
                return new SqlStringAndParameters(null, value, ((Collection<?>) value).size(), ValueTypeConstants.COLLECTION_TYPE);
            case ValueTypeConstants.ARRAY_TYPE:
                return new SqlStringAndParameters(null, value, ((Object[]) value).length, ValueTypeConstants.ARRAY_TYPE);
            case ValueTypeConstants.SELECT_MODEL_TYPE:
                SelectSqlEntity entity = MysqlHandler.handleSelect((SelectModel) value);
                return new SqlStringAndParameters(entity.getSelectSql(), entity.getValueList().stream().map(SqlParameter::getValue).collect(Collectors.toList()), entity.getValueList().size());
            case ValueTypeConstants.COLUMN_NAME_TYPE:
                BaseTableColumn baseTableColumn = (BaseTableColumn) value;
                return new SqlStringAndParameters(
                        SQLModelFactory.guardKeyword(baseTableColumn.getTableAlias()) + SqlCommonConstants.SQL_COMMON_DELIMITER + SQLModelFactory.guardKeyword(baseTableColumn.getColumnName()),
                        null,
                        0
                );
            case ValueTypeConstants.SQL_STRING_MODEL_TYPE:
                SqlStringModel sqlStringModel = (SqlStringModel) value;
                return new SqlStringAndParameters(sqlStringModel.getSql(), sqlStringModel.getValue());
            case ValueTypeConstants.CONDITION_RELATION_MODEL_TYPE:
                StringBuilder stringBuilder = new StringBuilder();
                List<SqlParameter> objectList = new ArrayList<>();
                handleRelation((RelationCondition) value, stringBuilder, objectList, true);
                return new SqlStringAndParameters(stringBuilder.toString(), objectList.stream().map(SqlParameter::getValue).collect(Collectors.toList()), objectList.size(), ValueTypeConstants.COLLECTION_TYPE);
            case ValueTypeConstants.SYMBOL_TYPE:
                return new SqlStringAndParameters(((SqlEquation.Symbol) value).getSymbol(), null);
            case ValueTypeConstants.SINGLE_EXPRESSION_CONDITION_MODEL:
                StringBuilder singleExpressionConditionSql = new StringBuilder();
                List<SqlParameter> singleExpressionConditionSqlParameterList = new ArrayList<>();
                handleSingleExpressionCondition((SingleExpressionCondition) value, singleExpressionConditionSql, singleExpressionConditionSqlParameterList);
                return new SqlStringAndParameters(singleExpressionConditionSql.toString(), singleExpressionConditionSqlParameterList.stream().map(SqlParameter::getValue).collect(Collectors.toList()));
            case ValueTypeConstants.MULTI_EXPRESSION_CONDITION_MODEL:
                StringBuilder multiExpressionConditionSql = new StringBuilder();
                List<SqlParameter> multiExpressionConditionSqlParameterList = new ArrayList<>();
                handleMultiExpressionCondition((MultiExpressionCondition) value, multiExpressionConditionSql, multiExpressionConditionSqlParameterList);
                return new SqlStringAndParameters(multiExpressionConditionSql.toString(), multiExpressionConditionSqlParameterList.stream().map(SqlParameter::getValue).collect(Collectors.toList()));
            case ValueTypeConstants.SQL_FUNCTION_CONDITION:
                return handleSqlFunctionCondition((SqlFunctionCondition) value);
            default:
                throw new IllegalArgumentException("不支持的类:" + value.getClass());
        }
    }

    private static void handleSqlFunctionCondition(SqlFunctionCondition sqlFunctionCondition, StringBuilder sql, List<SqlParameter> valueList) {
        SqlStringAndParameters sqlStringAndParameters = handleSqlFunctionCondition(sqlFunctionCondition);
        if (sqlStringAndParameters.isOnlyValue()) {
            sql.append(createPlaceholder(sqlStringAndParameters.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
        } else {
            sql.append(sqlStringAndParameters.getSql());
        }
        sqlStringAndParametersValueAddInList(sqlStringAndParameters, valueList);
    }

    private static SqlStringAndParameters handleSqlFunctionCondition(SqlFunctionCondition sqlFunctionCondition) {

        StringBuilder sqlFunction = new StringBuilder();
        List<SqlParameter> valueList = new ArrayList<>();

        sqlFunction.append(sqlFunctionCondition.getFunctionName());
        if (sqlFunctionCondition.isNeedBrackets()) {
            sqlFunction.append(SqlCommonConstants.LEFT_BRACKETS);
        }

        if (sqlFunctionCondition.getValues().length > 0) {
            SqlStringAndParameters sqlStringAndParameters = handleExpressionConditionValue(sqlFunctionCondition.getTypes()[0], sqlFunctionCondition.getValues()[0]);
            if (sqlStringAndParameters.isOnlyValue()) {
                sqlFunction.append(createPlaceholder(sqlStringAndParameters.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
            } else {
                sqlFunction.append(sqlStringAndParameters.getSql());
            }
            sqlStringAndParametersValueAddInList(sqlStringAndParameters, valueList);
            for (int i = 1; i < sqlFunctionCondition.getValues().length; i++) {
                sqlFunction.append(SqlCommonConstants.COMMA);
                sqlStringAndParameters = handleExpressionConditionValue(sqlFunctionCondition.getTypes()[i], sqlFunctionCondition.getValues()[i]);
                if (sqlStringAndParameters.isOnlyValue()) {
                    sqlFunction.append(createPlaceholder(sqlStringAndParameters.getPlaceholderNum(), SqlCommonConstants.PLACEHOLDER, SqlCommonConstants.COMMA));
                } else {
                    sqlFunction.append(sqlStringAndParameters.getSql());
                }
                sqlStringAndParametersValueAddInList(sqlStringAndParameters, valueList);
            }
        }
        if (sqlFunctionCondition.isNeedBrackets()) {
            sqlFunction.append(SqlCommonConstants.RIGHT_BRACKETS);
        }
        return new SqlStringAndParameters(sqlFunction.toString(), valueList.stream().map(SqlParameter::getValue).collect(Collectors.toList()));
    }


    /**
     * 将value加入列表
     *
     * @param valueList
     */
    private static void sqlStringAndParametersValueAddInList(SqlStringAndParameters sqlStringAndParameters, List<SqlParameter> valueList) {

        switch (sqlStringAndParameters.getValueType()) {
            case ValueTypeConstants.NULL_VALUE_MODEL:
                valueList.add(new SqlParameter(null, null));
                break;
            case ValueTypeConstants.BASE_VALUE_TYPE:
                valueList.add(new SqlParameter(null, sqlStringAndParameters.getValue()));
                break;
            case ValueTypeConstants.COLLECTION_TYPE:
                Collection<?> collection = (Collection<?>) sqlStringAndParameters.getValue();
                if (WsCollectionUtils.isNotEmpty(collection)) {
                    for (Object o : collection) {
                        valueList.add(new SqlParameter(null, o));
                    }
                }
                break;
            case ValueTypeConstants.ARRAY_TYPE:
                Object[] objects = (Object[]) sqlStringAndParameters.getValue();
                if (WsCollectionUtils.isNotEmpty(objects)) {
                    for (Object o : objects) {
                        valueList.add(new SqlParameter(null, o));
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 生成由分隔符分割的任意数量的占位符字符串
     *
     * @param placeholderSize 占位符数量
     * @param placeholderStr  占位符
     * @param separator       分隔符
     * @return
     */
    private static String createPlaceholder(int placeholderSize, String placeholderStr, String separator) {
        if (placeholderSize == 0) {
            return SqlCommonConstants.EMPTY_STRING;
        }
        if (placeholderSize == 1) {
            return placeholderStr;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < placeholderSize - 1; i++) {
            stringBuilder.append(placeholderStr)
                    .append(separator);
        }
        stringBuilder.append(placeholderStr);
        return stringBuilder.toString();
    }


    private static void handleUpdateCondition(List<Condition> conditionList) {

    }


}
