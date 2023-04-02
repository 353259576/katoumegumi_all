package ${packageName}${baseSearchVOName};

<#if enableSwagger = true>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if enableSpringDoc = true>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
import lombok.Data;
import java.io.Serializable;
<#list table.classList as cl>
import ${cl.getName()};
</#list>

<#if enableSwagger == true>
@ApiModel(value = "${table.tableRemark}查询类")
</#if>
<#if enableSpringDoc == true>
@Schema(description = "${table.tableRemark}查询类")
</#if>
@Data
public class ${table.entityName}SearchVO implements Serializable {

}
