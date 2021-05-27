package ${packageName}${baseSearchVOName};

<#if enableSwagger = true>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
import lombok.Data;
import java.io.Serializable;
<#list table.classList as cl>
import ${cl.getName()};
</#list>

<#if enableSwagger == true>
@ApiModel(value = "${table.tableRemark}查询类")
</#if>
@Data
public class ${table.entityName}SearchVO implements Serializable {

}
