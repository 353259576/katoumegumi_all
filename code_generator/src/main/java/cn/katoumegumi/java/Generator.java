package cn.katoumegumi.java;

import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.sql.HikariCPDataSourceFactory;
import cn.katoumegumi.java.utils.FreeMarkerUtils;
import cn.katoumegumi.java.utils.SqlTableToBeanUtils;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器
 * @author ws
 */
public class Generator {

    private static final String TEMPLATE_PATH = "cn/katoumegumi/java/service";

    public static final FreeMarkerUtils freeMarkerUtils = new FreeMarkerUtils(TEMPLATE_PATH);

    /**
     * 导出地址
     */
    private String exportPath;

    /**
     * 基础包名
     */
    private String packageName;


    private String packagePath;

    /**
     * java 代码文件夹
     */
    private String javaPath = "/src/main/java";

    /**
     * java 资源文件夹啊
     */
    private String resourcePath = "/src/main/resources";

    /**
     * 实体类的相对路径
     */
    private String entityPath = "/entity";

    private String baseEntityName = ".entity";

    /**
     * 接口的相对路径
     */
    private String servicePath = "/service";


    private String baseServiceName = ".service";

    /**
     * 结构实现的相对路径
     */
    private String serviceImplPath = "/service/impl";


    private String baseServiceImplName = ".service.impl";

    /**
     * 控制器的相对路径
     */
    private String controllerPath = "/controller";

    private String baseControllerName = ".controller";

    /**
     * mybatis mapper的相对路径
     */
    private String javaMapperPath = "/mapper";

    private String baseJavaMapperName = ".mapper";

    /**
     * mybatis xml 的相对路径
     */
    private String xmlMapperPath = "/mapper";

    /**
     * 查询类的相对路径
     */
    private String searchVOPath = "/vo/search";

    private String baseSearchVOName = ".vo.search";


    private Boolean enableSwagger = true;

    private Boolean enableMybatisPlus = true;

    private Boolean enableHibernate = true;
    /**
     * 0 sqlUtils 1 mybatisPlus 2 mybatis
     */
    private Integer type = 0;

    private Boolean enableSearchVO = true;

    private Boolean enableMybatis = true;

    private Boolean enableEntity = true;

    private Boolean enableService = true;

    private Boolean enableController = true;

    public Generator(String packageName, String exportPath) {
        this.packageName = packageName;
        this.exportPath = defaultSettingPath(exportPath);
        this.packagePath = "/" + packageName.replaceAll("\\.", "/");
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://47.96.119.77:3306/zs_jym_ms?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
        String userName = "root";
        String password = "Qq123456789.";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String dataBaseName = "zs_jym_ms";

        DataSource dataSource = HikariCPDataSourceFactory.getDataSource(url, userName, password, driverClassName);

        Generator generator = new Generator("cn.katoumegumi.java.lx", "D:\\project\\项目\\ws_all\\server_example");
        generator.setEntityPath("entity/model")
                .setServicePath("service")
                .setServiceImplPath("serviceImpl")
                .setControllerPath("admin.controller");
        SqlTableToBeanUtils sqlTableToBeanUtils = new SqlTableToBeanUtils(dataSource, dataBaseName, null);
        List<SqlTableToBeanUtils.Table> tableList = sqlTableToBeanUtils.selectTables("jym_store");
        for (SqlTableToBeanUtils.Table table : tableList) {
            generator.createEntity(table);
            generator.createSearchVO(table);
            generator.createService(table);
            generator.createServiceImpl(table);
            generator.createController(table);
            generator.createMybatisMapper(table);
            generator.createMybatisMapperJava(table);
        }
    }



    /**
     * 实体类
     * @param table
     */
    public void createEntity(SqlTableToBeanUtils.Table table) {
        if(!enableEntity){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("enableSwagger", this.enableSwagger);
        map.put("enableMybatisPlus", this.enableMybatisPlus);
        map.put("enableHibernate", this.enableHibernate);
        Template template = freeMarkerUtils.getTemplate("Entity.ftl");
        create(exportPath + javaPath + packagePath , entityPath, table.getEntityName() + ".java", template, table, map);
    }

    /**
     * 服务接口
     * @param table
     */
    public void createService(SqlTableToBeanUtils.Table table) {
        if(!enableService){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", this.type);
        Template template = freeMarkerUtils.getTemplate("Service.ftl");
        create(exportPath + javaPath + packagePath, servicePath, table.getEntityName() + "Service.java", template, table, map);
    }

    /**
     * 服务
     * @param table
     */
    public void createServiceImpl(SqlTableToBeanUtils.Table table) {
        if(!enableService){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("type", this.type);
        Template template = freeMarkerUtils.getTemplate("ServiceImpl.ftl");
        create(exportPath + javaPath + packagePath, serviceImplPath, table.getEntityName() + "ServiceImpl.java", template, table, map);
    }

    /**
     * 控制器
     * @param table
     */
    public void createController(SqlTableToBeanUtils.Table table) {
        if(!enableController){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("enableSwagger", this.enableSwagger);
        map.put("enableSearchVO",this.enableSearchVO);
        map.put("type",this.type);
        Template template = freeMarkerUtils.getTemplate("Controller.ftl");
        create(exportPath  + javaPath + packagePath , controllerPath, table.getEntityName() + "Controller.java", template, table, map);
    }

    /**
     * mybatisMapper.xml
     *
     * @param table
     */
    public void createMybatisMapper(SqlTableToBeanUtils.Table table) {
        if(!enableMybatisPlus && !enableMybatis){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("enableMybatisPlus", this.enableMybatisPlus);
        Template template = freeMarkerUtils.getTemplate("MybatisMapper.ftl");
        create(exportPath + resourcePath , xmlMapperPath, table.getEntityName() + "Mapper.xml", template, table, map);
    }

    /**
     * mybatisMapper.java
     *
     * @param table
     */
    public void createMybatisMapperJava(SqlTableToBeanUtils.Table table) {
        if(!(enableMybatis || enableMybatisPlus)){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("enableMybatisPlus", this.enableMybatisPlus);
        Template template = freeMarkerUtils.getTemplate("MybatisMapperJava.ftl");
        create(exportPath + javaPath + packagePath , javaMapperPath, table.getEntityName() + "Mapper.java", template, table, map);
    }

    public void createSearchVO(SqlTableToBeanUtils.Table table) {
        if(enableSearchVO) {
            Map<String, Object> map = new HashMap<>();
            map.put("enableMybatisPlus", this.enableMybatisPlus);
            map.put("enableSwagger", this.enableSwagger);
            Template template = freeMarkerUtils.getTemplate("SearchVO.ftl");
            create(exportPath + javaPath + packagePath, searchVOPath, table.getEntityName() + "SearchVO.java", template, table, map);
        }
    }


    private void create(String baseFilePath, String relativePackagePath,  String fileName, Template template, SqlTableToBeanUtils.Table table, Map<String, Object> map) {
        String filePath = baseFilePath + relativePackagePath;
        map.put("packageName", packageName);
        map.put("baseEntityName",baseEntityName);
        map.put("baseJavaMapperName",baseJavaMapperName);
        map.put("baseServiceImplName",baseServiceImplName);
        map.put("baseServiceName",baseServiceName);
        map.put("baseControllerName",baseControllerName);
        map.put("baseSearchVOName",baseSearchVOName);
        map.put("table", table);
        try {
            try {
                File file = WsFileUtils.createFile(filePath + "/" + fileName);
                FileWriter writer = new FileWriter(file);
                template.process(map, writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }



    public Generator setExportPath(String exportPath) {
        exportPath = defaultSettingPath(exportPath);
        this.exportPath = exportPath;
        return this;
    }

    public Generator setPackageName(String packageName) {
        this.packageName = packageName;
        this.packagePath = "/" + packageName.replaceAll("\\.", "/");
        return this;
    }

    public Generator setJavaPath(String javaPath) {
        defaultSettingPath(javaPath);
        this.javaPath = javaPath;
        return this;
    }

    public Generator setResourcePath(String resourcePath) {
        defaultSettingPath(resourcePath);
        this.resourcePath = resourcePath;
        return this;
    }

    public Generator setEntityPath(String entityPath) {
        entityPath = defaultSettingPath(entityPath);
        this.baseEntityName = defaultSettingName(entityPath);
        this.entityPath = entityPath;
        return this;
    }

    public Generator setServicePath(String servicePath) {
        servicePath = defaultSettingPath(servicePath);
        this.baseServiceName = defaultSettingName(servicePath);
        this.servicePath = servicePath;
        return this;
    }

    public Generator setServiceImplPath(String serviceImplPath) {
        serviceImplPath = defaultSettingPath(serviceImplPath);
        this.baseServiceImplName = defaultSettingName(serviceImplPath);
        this.serviceImplPath = serviceImplPath;
        return this;
    }

    public Generator setControllerPath(String controllerPath) {
        controllerPath = defaultSettingPath(controllerPath);
        this.baseControllerName = defaultSettingName(controllerPath);
        this.controllerPath = controllerPath;
        return this;
    }

    public Generator setJavaMapperPath(String javaMapperPath) {
        javaMapperPath = defaultSettingPath(javaMapperPath);
        this.baseJavaMapperName = defaultSettingName(javaMapperPath);
        this.javaMapperPath = javaMapperPath;
        return this;
    }


    public Generator setXmlMapperPath(String xmlMapperPath) {
        xmlMapperPath = defaultSettingPath(xmlMapperPath);
        this.xmlMapperPath = xmlMapperPath;
        return this;
    }



    public Generator setSearchVOPath(String searchVOPath) {
        searchVOPath =defaultSettingPath(searchVOPath);
        this.baseSearchVOName = defaultSettingName(searchVOPath);
        this.searchVOPath = searchVOPath;
        return this;
    }


    public Generator setEnableSwagger(Boolean enableSwagger) {
        this.enableSwagger = enableSwagger;
        return this;
    }


    public Generator setEnableMybatisPlus(Boolean enableMybatisPlus) {
        this.enableMybatisPlus = enableMybatisPlus;
        return this;
    }


    public Generator setEnableHibernate(Boolean enableHibernate) {
        this.enableHibernate = enableHibernate;
        return this;
    }


    public Generator setType(Integer type) {
        this.type = type;
        return this;
    }



    public Generator setEnableSearchVO(Boolean enableSearchVO) {
        this.enableSearchVO = enableSearchVO;
        return this;
    }

    public Generator setPackagePath(String packagePath) {
        this.packagePath = packagePath;
        return this;
    }

    public Generator setEnableMybatis(Boolean enableMybatis) {
        this.enableMybatis = enableMybatis;
        return this;
    }

    public Generator setEnableEntity(Boolean enableEntity) {
        this.enableEntity = enableEntity;
        return this;
    }

    public Generator setEnableService(Boolean enableService) {
        this.enableService = enableService;
        return this;
    }

    public Generator setEnableController(Boolean enableController) {
        this.enableController = enableController;
        return this;
    }

    private String defaultSettingPath(String str){
        if(WsStringUtils.isBlank(str)){
            return "";
        }
        str = str.replaceAll("\\\\","/");
        if(!str.startsWith("/")){
            str = "/" + str;
        }
        if(str.endsWith("/")){
            str = str.substring(0,str.length());
        }
        return str;
    }

    private String defaultSettingName(String str){
        if(WsStringUtils.isBlank(str)){
            return "";
        }
        return str.replaceAll("/",".");
    }
}
