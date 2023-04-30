package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.sql.entity.JdkResultSet;
import cn.katoumegumi.java.sql.entity.SqlParameter;
import cn.katoumegumi.java.sql.handle.MysqlHandle;
import cn.katoumegumi.java.sql.model.SelectModel;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * @author 星梦苍天
 */
public class BaseDataSourceUtils {


    private DataSource dataSource;

    public BaseDataSourceUtils(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public <T> List<T> selectList(MySearchList mySearchList) {
        SQLModelUtils sqlModelUtils = new SQLModelUtils(mySearchList);
        SelectSqlEntity entity = MysqlHandle.handleSelect(sqlModelUtils.transferToSelectModel());
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(entity.getSelectSql());
            List<Object> objectList = WsListUtils.listToList(entity.getValueList(), SqlParameter::getValue);
            for (int i = 0; i < objectList.size(); i++) {
                Object o = objectList.get(i);
                if (o instanceof Date) {
                    o = WsBeanUtils.objectToT(o, Date.class);
                }
                preparedStatement.setObject(i + 1, o);
            }
            resultSet = preparedStatement.executeQuery();
            return (List<T>) sqlModelUtils.margeMap(new JdkResultSet(resultSet));
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            WsStreamUtils.close(resultSet, preparedStatement, connection);
        }
        return null;
    }

    public <T> int insert(T t) {
        if (t == null) {
            throw new IllegalArgumentException("need insert Object is null");
        }
        SQLModelUtils sqlModelUtils = new SQLModelUtils(MySearchList.create(t.getClass()));
        InsertSqlEntity insertSqlEntity = sqlModelUtils.insertSql(t);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSqlEntity.getInsertSql(), Statement.RETURN_GENERATED_KEYS)) {
            List<SqlParameter> list = insertSqlEntity.getValueList();
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setObject(i + 1, list.get(i).getValue());
            }
            int updateRow = preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            List<FieldColumnRelation> idList = insertSqlEntity.getIdList();
            if (WsListUtils.isNotEmpty(idList) && resultSet != null) {
                int count = resultSet.getMetaData().getColumnCount();
                if (resultSet.next()) {
                    for (int i = 0; i < count && i < idList.size(); i++) {
                        WsFieldUtils.setValue(t, WsBeanUtils.objectToT(resultSet.getObject(i + 1), idList.get(i).getFieldClass()), idList.get(i).getField());
                    }
                }
            }
            return updateRow;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


}