package cn.katoumegumi.java;

import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.HikariCPDataSourceFactory;
import cn.katoumegumi.java.utils.FreeMarkerUtils;
import cn.katoumegumi.java.utils.SqlTableToBeanUtils;
import com.alibaba.fastjson.JSON;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.sql.DataSource;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Driver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ws
 */
public class Generator {

    private static final String TEMPLATE_PATH = "cn/katoumegumi/java/service";
    public static final FreeMarkerUtils freeMarkerUtils = new FreeMarkerUtils(TEMPLATE_PATH);

    private final String exportPath;

    private final String packageName;


    public Generator(String packageName,String exportPath){
        this.packageName = packageName;
        this.exportPath = exportPath;
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/wslx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
        String userName = "root";
        String password = "199645";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String dataBaseName = "wslx";

        DataSource dataSource = HikariCPDataSourceFactory.getDataSource(url,userName,password,driverClassName);

        Generator generator = new Generator("com.ws.lx","D://网页");
        SqlTableToBeanUtils sqlTableToBeanUtils = new SqlTableToBeanUtils(dataSource,dataBaseName);
        List<SqlTableToBeanUtils.Table> tableList = sqlTableToBeanUtils.selectTables(null);
        for(SqlTableToBeanUtils.Table table:tableList){
            generator.createEntity(table);
            generator.createService(table);
            generator.createServiceImpl(table);
            generator.createController(table);
            generator.createMybatisMapper(table);
        }

    }


    public void createEntity(SqlTableToBeanUtils.Table table){
        Template template = freeMarkerUtils.getTemplate("Entity.ftl");
        create("entity",table.getEntityName()+".java",template,table);
    }


    public void createService(SqlTableToBeanUtils.Table table){
        Template template = freeMarkerUtils.getTemplate("Service.ftl");
        create("service",table.getEntityName()+"Service.java",template,table);
    }

    public void createServiceImpl(SqlTableToBeanUtils.Table table){
        Template template = freeMarkerUtils.getTemplate("ServiceImpl.ftl");
        create("service/impl",table.getEntityName()+"ServiceImpl.java",template,table);
    }

    public void createController(SqlTableToBeanUtils.Table table){
        Template template = freeMarkerUtils.getTemplate("Controller.ftl");
        create("controller",table.getEntityName()+"Controller.java",template,table);
    }

    public void createMybatisMapper(SqlTableToBeanUtils.Table table){
        Template template = freeMarkerUtils.getTemplate("MybatisMapper.ftl");
        create("mapper",table.getEntityName()+"Mapper.xml",template,table);
    }


    private void create(String path,String fileName,Template template, SqlTableToBeanUtils.Table table){
        Map<String,Object> map = new HashMap<>();
        map.put("packageName",packageName);
        map.put("table",table);
        try {
            try {
                File file = WsFileUtils.createFile(exportPath+"/"+packageName.replaceAll("\\.","/") +"/"+path+"/"+fileName);
                FileWriter writer = new FileWriter(file);
                template.process(map,writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }


}
