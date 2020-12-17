package ${packageName}.entity;

<#if enableMybatisPlus == true>
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
</#if>
<#if enableSwagger = true>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
import lombok.Data;
<#if enableHibernate == true>
import javax.persistence.*;
</#if>
import java.io.Serializable;
<#list table.classList as cl>
import ${cl.getName()};
</#list>

<#if enableHibernate == true>
@Entity
@Table(name = "${table.tableName}")
</#if>
<#if enableMybatisPlus == true>
@TableName(value = "${table.tableName}")
</#if>
<#if enableSwagger == true>
@ApiModel(value = "${table.tableRemark}")
</#if>
@Data
public class ${table.entityName} implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * ${table.pkColumn.columnRemark}
    */
    <#if enableHibernate == true>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    <#if table.pkColumn.columnName == table.pkColumn.beanFieldName>
    <#if enableMybatisPlus == true>
    @TableId
    </#if>
        <#else>
    <#if enableMybatisPlus == true>
    @TableId(value="${table.pkColumn.columnName}")
    </#if>
    <#if enableHibernate == true>
    @Column(name = "${table.pkColumn.columnName}")
    </#if>
    </#if>
    <#if enableSwagger == true>
    @ApiModelProperty(value = "${table.pkColumn.columnRemark}")
    </#if>
    private ${table.pkColumn.columnClass.getSimpleName()} ${table.pkColumn.beanFieldName};

<#list table.columnList as column>
    <#if column != table.pkColumn>

    /**
    * ${column.columnRemark}
    */
        <#if enableMybatisPlus == true>
    @Column(name = "${column.columnName}")
        </#if>
        <#if enableHibernate == true>
    @TableField(value = "${column.columnName}")
        </#if>
        <#if enableSwagger == true>
    @ApiModelProperty(value = "${column.columnRemark}")
        </#if>
    private ${column.columnClass.getSimpleName()} ${column.beanFieldName};
    </#if>
</#list>
}
