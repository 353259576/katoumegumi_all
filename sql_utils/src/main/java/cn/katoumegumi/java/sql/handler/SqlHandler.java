package cn.katoumegumi.java.sql.handler;

import cn.katoumegumi.java.sql.handler.model.DeleteSqlEntity;
import cn.katoumegumi.java.sql.handler.model.SelectSqlEntity;
import cn.katoumegumi.java.sql.handler.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.model.result.DeleteModel;
import cn.katoumegumi.java.sql.model.result.SelectModel;
import cn.katoumegumi.java.sql.model.result.UpdateModel;

public interface SqlHandler {

    SelectSqlEntity select(SelectModel selectModel);

    DeleteSqlEntity delete(DeleteModel deleteModel);

    UpdateSqlEntity update(UpdateModel updateModel);
}
