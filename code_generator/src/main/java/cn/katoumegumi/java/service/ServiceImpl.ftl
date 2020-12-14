package ${packageName}.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<#if type == 0>
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.datasource.WsJdbcUtils;
</#if>
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ${packageName}.entity.${table.entityName};
import ${packageName}.service.${table.entityName}Service;
import ${table.pkColumn.columnClass.getName()};
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
<#if type == 1>
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${packageName}.mapper.${table.entityName}Mapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
</#if>

@Service
public class ${table.entityName}ServiceImpl<#if type == 1>  extends ServiceImpl<${table.entityName}Mapper,${table.entityName}></#if> implements ${table.entityName}Service {

<#if type == 0>
    @Resource
    private WsJdbcUtils jdbcUtils;
</#if>
<#if type == 1>
    @Resource
    private ${table.entityName}Mapper ${table.firstLowerEntityName}Mapper;
</#if>


    /**
    * 增加
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer insert(${table.entityName} ${table.firstLowerEntityName}){
<#if type == 0>
        return jdbcUtils.insert(${table.firstLowerEntityName});
</#if>
<#if type == 1>
        return ${table.firstLowerEntityName}Mapper.insert(${table.firstLowerEntityName});
</#if>
    }

    /**
    * 修改
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer update(${table.entityName} ${table.firstLowerEntityName}){
<#if type == 0>
        return jdbcUtils.update(${table.firstLowerEntityName});
</#if>
<#if type == 1>
        return ${table.firstLowerEntityName}Mapper.updateById(${table.firstLowerEntityName});
</#if>
    }

    /**
    * 批量增加或修改
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void insertOrUpdateBatch(List<${table.entityName}> ${table.firstLowerEntityName}List){
        List<${table.entityName}> insert = new ArrayList<>();
        List<${table.entityName}> update = new ArrayList<>();
        for(${table.entityName} ${table.firstLowerEntityName}:${table.firstLowerEntityName}List){
            if(${table.firstLowerEntityName}.getId() == null){
                insert(${table.firstLowerEntityName});
            }else {
                update(${table.firstLowerEntityName});
            }
        }
    }

    /**
    * 分页查询
    */
    @Override
    public IPage<${table.entityName}> selectPage(<#if type == 0>MySearchList searchList</#if><#if type == 1>Page page,Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        IPage<${table.entityName}> ${table.firstLowerEntityName}Page = jdbcUtils.getTPage(searchList);
        return ${table.firstLowerEntityName}Page;
</#if>
<#if type == 1>
        IPage<${table.entityName}> ${table.firstLowerEntityName}Page = ${table.firstLowerEntityName}Mapper.selectPage(page,wrapper);
        return ${table.firstLowerEntityName}Page;
</#if>
    }

    /**
    * 列表查询
    */
    @Override
    public List<${table.entityName}> selectList(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        List<${table.entityName}> ${table.firstLowerEntityName}List = jdbcUtils.getListT(searchList);
        return ${table.firstLowerEntityName}List;
</#if>
<#if type == 1>
        List<${table.entityName}> ${table.firstLowerEntityName}List = ${table.firstLowerEntityName}Mapper.selectList(wrapper);
        return ${table.firstLowerEntityName}List;
</#if>
    }

    /**
    * 单条查询
    */
    @Override
    public ${table.entityName} select(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        ${table.entityName} ${table.firstLowerEntityName} = jdbcUtils.getTOne(searchList);
        return ${table.firstLowerEntityName};
</#if>
<#if type == 1>
        ${table.entityName} ${table.firstLowerEntityName} = ${table.firstLowerEntityName}Mapper.selectOne(wrapper);
        return ${table.firstLowerEntityName};
</#if>
    }

    /**
    * 删除
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer delete(${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
<#if type == 0>
        return jdbcUtils.update(MySearchList.create(${table.entityName}.class).set("","").eq("${table.pkColumn.beanFieldName}",${table.pkColumn.beanFieldName}));
</#if>
<#if type == 1>
        ${table.entityName} ${table.firstLowerEntityName} = new ${table.entityName}();
</#if>
    }

}