package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
<#list table.classList as cl>
import ${cl.getName()};
</#list>

@Entity
@Table(name = "${table.tableName}")
@Data
@TableName(value = "${table.tableName}")
@ApiModel(value = "${table.tableRemark}")
public class ${table.entityName} implements Serializable {


    private static final long serialVersionUID = 1L;

<#list table.columnList as column>

    /**
    * ${column.columnRemark}
    */
    <#if column.columnKey == "PRI">
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    <#if column.columnName == column.beanFieldName>
    @TableId
        <#else>
    @TableId(value="${column.columnName}")
    @Column(name = "${column.columnName}")
    </#if>
        <#else >
    @Column(name = "${column.columnName}")
    @TableField(value = "${column.columnName}")
    </#if>
    @ApiModelProperty(value = "${column.columnRemark}")
    private ${column.columnClass.getSimpleName()} ${column.beanFieldName};

</#list>
}
