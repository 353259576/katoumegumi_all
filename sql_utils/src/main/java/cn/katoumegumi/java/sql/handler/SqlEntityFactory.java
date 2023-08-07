package cn.katoumegumi.java.sql.handler;

import cn.katoumegumi.java.sql.handler.model.DeleteSqlEntity;
import cn.katoumegumi.java.sql.handler.model.SelectSqlEntity;
import cn.katoumegumi.java.sql.handler.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.model.result.DeleteModel;
import cn.katoumegumi.java.sql.model.result.SelectModel;
import cn.katoumegumi.java.sql.model.result.UpdateModel;

public class SqlEntityFactory {

    private static final SqlHandler SQL_HANDLER = new MysqlSqlHandler();

    /**
     * 生成查询语句
     * @param selectModel
     * @return
     */
    public static SelectSqlEntity createSelectSqlEntity(SelectModel selectModel) {
        return SQL_HANDLER.select(selectModel);
    }

    /**
     * 生成删除语句
     * @param deleteModel
     * @return
     */
    public static DeleteSqlEntity createDeleteSqlEntity(DeleteModel deleteModel) {
        return SQL_HANDLER.delete(deleteModel);
    }

    /**
     * 生成修改语句
     * @param updateModel
     * @return
     */
    public static UpdateSqlEntity createUpdateSqlEntity(UpdateModel updateModel) {
        return SQL_HANDLER.update(updateModel);
    }


}
