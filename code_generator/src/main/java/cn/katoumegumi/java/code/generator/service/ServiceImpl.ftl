package ${packageName}${baseServiceImplName};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<#if type == 0>
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.starter.jdbc.datasource.WsJdbcUtils;
<#if enableSearchVO == true>
import ${packageName}${baseSearchVOName}.${table.entityName}SearchVO;
</#if>
</#if>
import cn.katoumegumi.java.common.WsCollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import ${packageName}${baseEntityName}.${table.entityName};
import ${packageName}${baseServiceName}.${table.entityName}Service;
import ${table.pkColumn.columnClass.getName()};
import java.util.Collections;
import java.util.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
<#if type == 1>
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${packageName}${baseJavaMapperName}.${table.entityName}Mapper;
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
    public Integer save(${table.entityName} ${table.firstLowerEntityName}){
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
    public void saveOrUpdateBatch(List<${table.entityName}> ${table.firstLowerEntityName}List){
        for(${table.entityName} ${table.firstLowerEntityName}:${table.firstLowerEntityName}List){
            if(${table.firstLowerEntityName}.getId() == null){
                save(${table.firstLowerEntityName});
            }else {
                update(${table.firstLowerEntityName});
            }
        }
    }

    /**
    * 分页查询
    */
    @Override
    public IPage<${table.entityName}> queryPage(<#if type == 0>MySearchList searchList</#if><#if type == 1>Page page,Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        IPage<${table.entityName}> ${table.firstLowerEntityName}Page = jdbcUtils.getTPage(searchList);
        wrapper${table.entityName}(${table.firstLowerEntityName}Page.getRecords());
        return ${table.firstLowerEntityName}Page;
</#if>
<#if type == 1>
        IPage<${table.entityName}> ${table.firstLowerEntityName}Page = ${table.firstLowerEntityName}Mapper.selectPage(page,wrapper);
        wrapper${table.entityName}(${table.firstLowerEntityName}Page.getRecords());
        return ${table.firstLowerEntityName}Page;
</#if>
    }

    /**
    * 列表查询
    */
    @Override
    public List<${table.entityName}> queryList(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        List<${table.entityName}> ${table.firstLowerEntityName}List = jdbcUtils.getListT(searchList);
        wrapper${table.entityName}(${table.firstLowerEntityName}List);
        return ${table.firstLowerEntityName}List;
</#if>
<#if type == 1>
        List<${table.entityName}> ${table.firstLowerEntityName}List = ${table.firstLowerEntityName}Mapper.selectList(wrapper);
        wrapper${table.entityName}(${table.firstLowerEntityName}List);
        return ${table.firstLowerEntityName}List;
</#if>
    }

    /**
    * 单条查询
    */
    @Override
    public ${table.entityName} queryEntity(<#if type == 0>MySearchList searchList</#if><#if type == 1>Wrapper<${table.entityName}> wrapper</#if>){
<#if type == 0>
        ${table.entityName} ${table.firstLowerEntityName} = jdbcUtils.getTOne(searchList);
        if(${table.firstLowerEntityName} == null){
            return null;
        }
        wrapper${table.entityName}(Collections.singletonList(${table.firstLowerEntityName}));
        return ${table.firstLowerEntityName};
</#if>
<#if type == 1>
        ${table.entityName} ${table.firstLowerEntityName} = ${table.firstLowerEntityName}Mapper.selectOne(wrapper);
        if(${table.firstLowerEntityName} == null){
            return null;
        }
        wrapper${table.entityName}(Collections.singletonList(${table.firstLowerEntityName}));
        return ${table.firstLowerEntityName};
</#if>
    }

    @Override
    public List<${table.entityName}> queryListByIds(Collection<${table.pkColumn.columnClass.getSimpleName()}> ids){
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
    <#if type ==0>
        MySearchList searchList = MySearchList.create(${table.entityName}.class).in("${table.pkColumn.beanFieldName}",ids);
        List<${table.entityName}> list = queryList(searchList);
    </#if>
    <#if type == 1>
        Wrapper<${table.entityName}> wrapper = Wrappers.query(${table.entityName}.class).in("${table.pkColumn.beanFieldName}",ids);
        List<${table.entityName}> list = queryList(wrapper);
    </#if>
        return list;
    }

    /**
    * 删除
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer remove(${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
<#if type == 0>
        return jdbcUtils.update(MySearchList.create(${table.entityName}.class).set("","").eq("${table.pkColumn.beanFieldName}",${table.pkColumn.beanFieldName}));
</#if>
<#if type == 1>
        ${table.entityName} ${table.firstLowerEntityName} = new ${table.entityName}();
</#if>
    }

<#if type == 0 && enableSearchVO == true>
    @Override
    public MySearchList analysisSearchVo(${table.entityName}SearchVO searchVO){
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        return searchList;
    }
</#if>

    /**
    * 包装
    */
    private void wrapper${table.entityName}(List<${table.entityName}> ${table.firstLowerEntityName}List) {
        if(WsListUtils.isEmpty(${table.firstLowerEntityName}List)) {
            return;
        }
    }

}