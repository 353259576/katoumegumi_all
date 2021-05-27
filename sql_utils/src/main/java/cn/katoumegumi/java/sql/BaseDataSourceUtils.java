package cn.katoumegumi.java.sql;

import cn.katoumegumi.java.common.WsBeanUtils;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class BaseDataSourceUtils {


    private DataSource dataSource;

    public BaseDataSourceUtils(DataSource dataSource){
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
            List<Object> objectList = entity.getValueList();
            for(int i = 0; i < objectList.size(); i++){
                Object o = objectList.get(i);
                if(o instanceof Date){
                    o = WsBeanUtils.objectToT(o,Date.class);
                }
                preparedStatement.setObject(i+1,o);
            }
            resultSet = preparedStatement.executeQuery();

            return (List<T>) sqlModelUtils.margeMap(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            if(resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if(preparedStatement != null){
                try {
                    preparedStatement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return null;
    }



}
