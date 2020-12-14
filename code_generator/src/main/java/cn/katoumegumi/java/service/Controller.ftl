package ${packageName}.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<#if type == 0>
import cn.katoumegumi.java.sql.MySearchList;
</#if>
<#if type == 1>
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
</#if>
import ${packageName}.entity.${table.entityName};
import ${packageName}.service.${table.entityName}Service;
import ${table.pkColumn.columnClass.getName()};
import org.springframework.web.bind.annotation.*;
<#if enableSwagger == true>
import io.swagger.annotations.*;
</#if>
import java.util.List;
import javax.annotation.Resource;

@RestController
<#if enableSwagger == true>
@Api(description = "")
</#if>
@RequestMapping(value = "/${table.entityName}/")
public class ${table.entityName}Controller {

    @Resource
    private ${table.entityName}Service ${table.firstLowerEntityName}Service;

    /**
    * 增加
    */
    @PostMapping(value = "insert")
<#if enableSwagger == true>
    @ApiOperation("创建")
</#if>
    public Integer insert(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.insert(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 修改
    */
    @PutMapping(value = "update")
<#if enableSwagger == true>
    @ApiOperation("修改")
</#if>
    public Integer update(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.update(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 批量增加或修改
    */
    @PostMapping(value = "insertOrUpdateBatch")
<#if enableSwagger == true>
    @ApiOperation("批量创建修改")
</#if>
    public Integer insertOrUpdateBatch(@RequestBody List<${table.entityName}> ${table.firstLowerEntityName}List){
        ${table.firstLowerEntityName}Service.insertOrUpdateBatch(${table.firstLowerEntityName}List);
        return null;
    }

    /**
    * 分页查询
    */
    @GetMapping(value = "selectPage")
<#if enableSwagger == true>
    @ApiOperation("分页查询")
</#if>
    public IPage<${table.entityName}> selectPage(Page<?> page){
<#if type == 0>
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        searchList.setPageVO(page);
        IPage<${table.entityName}> iPage = ${table.firstLowerEntityName}Service.selectPage(searchList);
</#if>
<#if type == 1>
        Wrapper<${table.entityName}> wrapper = Wrappers.lambdaUpdate(${table.entityName}.class);
        IPage<${table.entityName}> iPage = ${table.firstLowerEntityName}Service.selectPage(page,wrapper);
</#if>
        return null;
    }

    /**
    * 列表查询
    */
    @GetMapping(value = "selectList")
<#if enableSwagger == true>
    @ApiOperation("列表查询")
</#if>
    public List<${table.entityName}> selectList(){
<#if type ==0>
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        List<${table.entityName}> list = ${table.firstLowerEntityName}Service.selectList(searchList);
</#if>
<#if type == 1>
        Wrapper<${table.entityName}> wrapper = Wrappers.lambdaUpdate(${table.entityName}.class);
        List<${table.entityName}> list = ${table.firstLowerEntityName}Service.selectList(wrapper);
</#if>
        return null;
    }

    /**
    * 删除
    */
    @DeleteMapping(value = "delete/{${table.pkColumn.beanFieldName}}")
<#if enableSwagger == true>
    @ApiOperation("删除")
</#if>
    public Integer delete(@PathVariable ${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
        ${table.firstLowerEntityName}Service.delete(${table.pkColumn.beanFieldName});
        return null;
    }
}
