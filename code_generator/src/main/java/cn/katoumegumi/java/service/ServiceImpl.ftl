package ${packageName}.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.datasource.WsJdbcUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ${packageName}.entity.${table.entityName};
import ${packageName}.service.${table.entityName}Service;
import ${table.pkColumn.columnClass.getName()};
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
public class ${table.entityName}ServiceImpl implements ${table.entityName}Service{

    @Resource
    private WsJdbcUtils jdbcUtils;

    /**
    * 增加
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer insert(${table.entityName} ${table.firstLowerEntityName}){
        return jdbcUtils.insert(${table.firstLowerEntityName});
    }

    /**
    * 修改
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer update(${table.entityName} ${table.firstLowerEntityName}){
        return jdbcUtils.update(${table.firstLowerEntityName});
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
                insert.add(${table.firstLowerEntityName});
            }else {
                update.add(${table.firstLowerEntityName});
            }
        }
        if(!CollectionUtils.isEmpty(insert)){
            jdbcUtils.insert(insert);
        }
        if(!CollectionUtils.isEmpty(update)){
            jdbcUtils.updateBatchByT(update);
        }
    }

    /**
    * 分页查询
    */
    @Override
    public IPage<${table.entityName}> selectPage(MySearchList searchList){
        IPage<${table.entityName}> ${table.firstLowerEntityName}Page = jdbcUtils.getTPage(searchList);
        return ${table.firstLowerEntityName}Page;
    }

    /**
    * 列表查询
    */
    @Override
    public List<${table.entityName}> selectList(MySearchList searchList){
        List<${table.entityName}> ${table.firstLowerEntityName}List = jdbcUtils.getListT(searchList);
        return ${table.firstLowerEntityName}List;
    }

    /**
    * 单条查询
    */
    @Override
    public ${table.entityName} select(MySearchList searchList){
        ${table.entityName} ${table.firstLowerEntityName} = jdbcUtils.getTOne(searchList);
        return ${table.firstLowerEntityName};
    }

    /**
    * 删除
    */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer delete(${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName}){
        return jdbcUtils.update(MySearchList.create(${table.entityName}.class).set("","").eq("${table.pkColumn.beanFieldName}",${table.pkColumn.beanFieldName}));
    }
}