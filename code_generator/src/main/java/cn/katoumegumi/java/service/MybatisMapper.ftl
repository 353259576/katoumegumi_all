<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${table.entityName}Mapper">


    <resultMap id="${table.firstLowerEntityName}BaseResultMap" type="${packageName}.model.${table.entityName}">
        <id property="${table.pkColumn.beanFieldName}" column="${table.pkColumn.columnName}"/>
        <#list table.columnList as column>
            <#if column.columnKey != "PRI">
        <result property="${column.beanFieldName}" column="${column.columnName}"/>
            </#if>
        </#list>
    </resultMap>


    <sql id="base${table.entityName}Sql">
        <#list table.columnList as column>
            ${column.columnName} as ${column.beanFieldName}<#if column_has_next>,</#if>
        </#list>
    </sql>

    <select id="select${table.entityName}List" resultMap="${table.firstLowerEntityName}BaseResultMap">
        <include refid="base${table.entityName}Sql"/>
        from ${table.tableName}
    </select>

    <insert id="insert${table.entityName}" parameterType="${packageName}.model.${table.entityName}">
        insert into ${table.tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
        <#list table.columnList as column>
        <if test="${column.beanFieldName} != null">
        ${column.columnName},
        </if>
        </#list>
        </trim>
        values
        <trim prefix="(" suffix=")" suffixOverrides=",">
        <#list table.columnList as column>
        <if test="${column.beanFieldName} != null">
        #\{${column.columnName}\},
        </if>
        </#list>
        </trim>
    </insert>


    <update id="update${table.entityName}" parameterType="${packageName}.model.${table.entityName}">
        UPDATE ${table.tableName} SET
        <trim suffixOverrides=",">
        <#list table.columnList as column>
        <if test="${column.beanFieldName} != null">
        <#if column.columnType != "PRI">
        ${column.columnName} = #\{${column.beanFieldName}\},
        </#if>
        </if>
        </#list>
        </trim>
        where
        ${table.pkColumn.columnName} = #\{${table.pkColumn.beanFieldName}\}
    </update>

</mapper>
