package ${packageName}.service;

import cn.exrick.xboot.core.entity.ApesStoreCar;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.sql.MySearchList;
import ${packageName}.entity.${table.entityName};
import ${table.pkColumn.columnClass.getName()};
import java.util.List;

public interface ${table.entityName}Service {

    /**
    * 增加
    */
    public Integer insert(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 修改
    */
    public Integer update(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 批量增加或修改
    */
    public void insertOrUpdateBatch(List<${table.entityName}> ${table.firstLowerEntityName}List);

    /**
    * 分页查询
    */
    public IPage<${table.entityName}> selectPage(MySearchList searchList);

    /**
    * 列表查询
    */
    public List<${table.entityName}> selectList(MySearchList searchList);

    /**
    * 单条查询
    */
    public ${table.entityName} select(MySearchList searchList);

    /**
    * 删除
    */
    public Integer delete(${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName});
}
