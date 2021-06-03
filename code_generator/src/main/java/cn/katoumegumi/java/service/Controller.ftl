package ${packageName}${baseControllerName};

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
<#if type == 0>
import cn.katoumegumi.java.sql.MySearchList;
</#if>
<#if type == 1>
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
</#if>
import ${packageName}${baseEntityName}.${table.entityName};
import ${packageName}${baseServiceName}.${table.entityName}Service;
<#if enableSearchVO == true>
import ${packageName}${baseSearchVOName}.${table.entityName}SearchVO;
</#if>
import ${table.pkColumn.columnClass.getName()};
import org.springframework.web.bind.annotation.*;
<#if enableSwagger == true>
import io.swagger.annotations.*;
</#if>
import java.util.List;
import javax.annotation.Resource;


/**
* ${table.tableRemark}Controller
*/

@RestController
<#if enableSwagger == true>
@Api(value="${table.firstLowerEntityName}", tags = "${table.tableRemark}controller")
</#if>
@RequestMapping(value = "/${table.firstLowerEntityName}/")
public class ${table.entityName}Controller {

    @Resource
    private ${table.entityName}Service ${table.firstLowerEntityName}Service;

    /**
    * 增加${table.tableRemark}
    */
    @PostMapping(value = "save")
<#if enableSwagger == true>
    @ApiOperation("创建${table.tableRemark}")
</#if>
    public Integer save(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.save(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 修改${table.tableRemark}
    */
    @PutMapping(value = "update")
<#if enableSwagger == true>
    @ApiOperation("修改${table.tableRemark}")
</#if>
    public Integer update(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.update(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 批量增加或修改${table.tableRemark}
    */
    @PostMapping(value = "saveOrUpdateBatch")
<#if enableSwagger == true>
    @ApiOperation("批量创建修改${table.tableRemark}")
</#if>
    public Integer saveOrUpdateBatch(@RequestBody List<${table.entityName}> ${table.firstLowerEntityName}List){
        ${table.firstLowerEntityName}Service.saveOrUpdateBatch(${table.firstLowerEntityName}List);
        return null;
    }

    /**
    * 分页查询${table.tableRemark}
    */
    @GetMapping(value = "get${table.entityName}Page")
<#if enableSwagger == true>
    @ApiOperation("分页查询${table.tableRemark}")
</#if>
    public IPage<${table.entityName}> get${table.entityName}Page(Page<?> page,<#if enableSearchVO == true>${table.entityName}SearchVO searchVO</#if>){
<#if type == 0>
        MySearchList searchList = getMySearchList(searchVO);
        searchList.setSqlLimit(sqlLimit -> sqlLimit.setCurrent(page.getCurrent()).setSize(page.getSize()));
        IPage<${table.entityName}> iPage = ${table.firstLowerEntityName}Service.queryPage(searchList);
</#if>
<#if type == 1>
        Wrapper<${table.entityName}> wrapper = getWrapper(searchVO);
        IPage<${table.entityName}> iPage = ${table.firstLowerEntityName}Service.queryPage(page,wrapper);
</#if>
        return null;
    }

    /**
    * 列表查询${table.tableRemark}
    */
    @GetMapping(value = "get${table.entityName}List")
<#if enableSwagger == true>
    @ApiOperation("列表查询${table.tableRemark}")
</#if>
    public List<${table.entityName}> get${table.entityName}List(<#if enableSearchVO == true>${table.entityName}SearchVO searchVO</#if>){
<#if type ==0>
        MySearchList searchList = getMySearchList(searchVO);
        List<${table.entityName}> list = ${table.firstLowerEntityName}Service.queryList(searchList);
</#if>
<#if type == 1>
        Wrapper<${table.entityName}> wrapper = getWrapper(searchVO);
        List<${table.entityName}> list = ${table.firstLowerEntityName}Service.queryList(wrapper);
</#if>
        return null;
    }

    /**
    * 删除${table.tableRemark}
    */
    @DeleteMapping(value = "remove/{${table.pkColumn.beanFieldName}}")
<#if enableSwagger == true>
    @ApiOperation("删除${table.tableRemark}")
</#if>
    public Integer remove(@PathVariable ${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
        ${table.firstLowerEntityName}Service.remove(${table.pkColumn.beanFieldName});
        return null;
    }

<#if type == 0>
    private MySearchList getMySearchList(<#if enableSearchVO == true>${table.entityName}SearchVO searchVO</#if>) {
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        return searchList;
    }
</#if>
<#if type == 1>
    private Wrapper<${table.entityName}> getWrapper(<#if enableSearchVO == true>${table.entityName}SearchVO searchVO</#if>) {
        Wrapper<${table.entityName}> wrapper = Wrappers.lambdaUpdate(${table.entityName}.class);
        return wrapper;
    }
</#if>

}
