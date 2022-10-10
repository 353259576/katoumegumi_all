package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStreamUtils;
import cn.katoumegumi.java.sql.entity.JdkResultSet;
import cn.katoumegumi.java.sql.entity.SqlParameter;

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
        SelectSqlEntity entity = sqlModelUtils.select();

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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            WsStreamUtils.close(resultSet,preparedStatement,connection);
        }
        return null;
    }


}
