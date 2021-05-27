package ${packageName}${baseServiceName};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ${packageName}${baseEntityName}.${table.entityName};
import ${table.pkColumn.columnClass.getName()};
import java.util.List;
<#if type == 0>
import cn.katoumegumi.java.sql.MySearchList;
</#if>
<#if type ==1>
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
</#if>

public interface ${table.entityName}Service<#if type == 2> extends IService<${table.entityName}></#if> {

    /**
    * 增加
    */
    public Integer save(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 修改
    */
    public Integer update(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 批量增加或修改
    */
    public void saveOrUpdateBatch(List<${table.entityName}> ${table.firstLowerEntityName}List);

    /**
    * 分页查询
    */
    public IPage<${table.entityName}> queryPage(<#if type == 0>MySearchList searchList</#if><#if type == 1>Page page,Wrapper<${table.entityName}> wrapper</#if>);

    /**
    * 列表查询
    */
    public List<${table.entityName}> queryList(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>);

    /**
    * 单条查询
    */
    public ${table.entityName} queryEntity(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>);

    /**
    * 删除
    */
    public Integer remove(${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName});

}
