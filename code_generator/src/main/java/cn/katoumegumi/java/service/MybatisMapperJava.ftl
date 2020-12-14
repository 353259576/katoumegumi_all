package ${packageName}.mapper;

import java.util.List;
import ${packageName}.entity.${table.entityName};
<#if enableMybatisPlus == true>
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
</#if>

public interface ${table.entityName}Mapper<#if enableMybatisPlus == true> extends BaseMapper<${table.entityName}></#if> {

<#if enableMybatisPlus == false>
    /**
    * 创建
    */
    public Integer insert${table.entityName}(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 创建
    */
    public Integer update${table.entityName}(${table.entityName} ${table.firstLowerEntityName});

    /**
    * 列表查询
    */
    public List<${table.entityName}> select${table.entityName}List();
</#if>

}
