package ${packageName}.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.sql.MySearchList;
import ${packageName}.entity.${table.entityName};
import ${packageName}.service.${table.entityName}Service;
import ${table.pkColumn.columnClass.getName()};
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.util.List;
import javax.annotation.Resource;

@RestController
@Api(description = "")
@RequestMapping(value = "/${table.entityName}/")
public class ${table.entityName}Controller {

    @Resource
    private ${table.entityName}Service ${table.firstLowerEntityName}Service;

    /**
    * 增加
    */
    @PostMapping(value = "insert")
    @ApiOperation("创建")
    @SystemLog(description = "创建")
    public Integer insert(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.insert(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 修改
    */
    @PutMapping(value = "update")
    @ApiOperation("修改")
    @SystemLog(description = "修改")
    public Integer update(@RequestBody ${table.entityName} ${table.firstLowerEntityName}){
        ${table.firstLowerEntityName}Service.update(${table.firstLowerEntityName});
        return null;
    }

    /**
    * 批量增加或修改
    */
    @PostMapping(value = "insertOrUpdateBatch")
    @ApiOperation("批量创建修改")
    @SystemLog(description = "批量创建修改")
    public Integer insertOrUpdateBatch(@RequestBody List<${table.entityName}> ${table.firstLowerEntityName}List){
        ${table.firstLowerEntityName}Service.insertOrUpdateBatch(${table.firstLowerEntityName}List);
        return null;
    }

    /**
    * 分页查询
    */
    @GetMapping(value = "selectPage")
    @ApiOperation("分页查询")
    public IPage<${table.entityName}> selectPage(Page<?> page){
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        searchList.setPageVO(page);
        IPage<${table.entityName}> iPage = ${table.firstLowerEntityName}Service.selectPage(searchList);
        return null;
    }

    /**
    * 列表查询
    */
    @GetMapping(value = "selectList")
    @ApiOperation("列表查询")
    public List<${table.entityName}> selectList(){
        MySearchList searchList = MySearchList.create(${table.entityName}.class);
        List<${table.entityName}> list = ${table.firstLowerEntityName}Service.selectList(searchList);
        return null;
    }

    /**
    * 删除
    */
    @DeleteMapping(value = "delete/{${table.pkColumn.beanFieldName}}")
    @ApiOperation("删除")
    @SystemLog(description = "删除")
    public Integer delete(@PathVariable ${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
        ${table.firstLowerEntityName}Service.delete(${table.pkColumn.beanFieldName});
        return null;
    }
}
