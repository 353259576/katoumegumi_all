package cn.katoumegumi.java.sql.handle;

import cn.katoumegumi.java.sql.handle.model.DeleteSqlEntity;
import cn.katoumegumi.java.sql.handle.model.SelectSqlEntity;
import cn.katoumegumi.java.sql.handle.model.UpdateSqlEntity;
import cn.katoumegumi.java.sql.model.result.DeleteModel;
import cn.katoumegumi.java.sql.model.result.SelectModel;
import cn.katoumegumi.java.sql.model.result.UpdateModel;

public interface SqlHandler {

    SelectSqlEntity select(SelectModel selectModel);

    DeleteSqlEntity delete(DeleteModel deleteModel);

    UpdateSqlEntity update(UpdateModel updateModel);
}
