package cn.katoumegumi.java;

import cn.katoumegumi.java.common.WsFileUtils;
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
 * @author ws
 */
public class Generator {

    private static final String TEMPLATE_PATH = "cn/katoumegumi/java/service";
    public static final FreeMarkerUtils freeMarkerUtils = new FreeMarkerUtils(TEMPLATE_PATH);

    private final String exportPath;

    private final String packageName;

    private final String packagePath;

    private final String javaPath = "src/main/java";

    private final String resourcePath = "src/main/resources";


    public Generator(String packageName, String exportPath) {
        this.packageName = packageName;
        this.exportPath = exportPath;
        this.packagePath = packageName.replaceAll("\\.", "/");
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/wslx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
        String userName = "root";
        String password = "199645";
        String driverClassName = "com.mysql.cj.jdbc.Driver";
        String dataBaseName = "wslx";

        DataSource dataSource = HikariCPDataSourceFactory.getDataSource(url, userName, password, driverClassName);

        Generator generator = new Generator("cn.katoumegumi.java.lx", "D:\\project\\项目\\ws_all\\server_example");
        SqlTableToBeanUtils sqlTableToBeanUtils = new SqlTableToBeanUtils(dataSource, dataBaseName, null);
        List<SqlTableToBeanUtils.Table> tableList = sqlTableToBeanUtils.selectTables("ws_user");
        for (SqlTableToBeanUtils.Table table : tableList) {
            generator.createEntity(table, false, true, true);
            generator.createService(table, 0);
            generator.createServiceImpl(table, 0);
            generator.createController(table, false,0);
            generator.createMybatisMapper(table, true);
            generator.createMybatisMapperJava(table, true);
        }

    }

    /**
     * 实体类
     *
     * @param table
     */
    public void createEntity(SqlTableToBeanUtils.Table table, boolean enableSwagger, boolean enableMybatisPlus, boolean enableHibernate) {
        Map<String, Object> map = new HashMap<>();
        map.put("enableSwagger", enableSwagger);
        map.put("enableMybatisPlus", enableMybatisPlus);
        map.put("enableHibernate", enableHibernate);
        Template template = freeMarkerUtils.getTemplate("Entity.ftl");
        create(exportPath + "/" + javaPath + "/" + packagePath + "/entity", table.getEntityName() + ".java", template, table, map);
    }

    /**
     * 服务接口
     *
     * @param table
     * @param type  0 sqlUtils 1 mybatisPlus 2 mybatis
     */
    public void createService(SqlTableToBeanUtils.Table table, int type) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        Template template = freeMarkerUtils.getTemplate("Service.ftl");
        create(exportPath + "/" + javaPath + "/" + packagePath + "/service", table.getEntityName() + "Service.java", template, table, map);
    }

    /**
     * 服务
     *
     * @param table
     * @param type  0 sqlUtils 1 mybatisPlus 2 mybatis
     */
    public void createServiceImpl(SqlTableToBeanUtils.Table table, int type) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        Template template = freeMarkerUtils.getTemplate("ServiceImpl.ftl");
        create(exportPath + "/" + javaPath + "/" + packagePath + "/service/impl", table.getEntityName() + "ServiceImpl.java", template, table, map);
    }

    /**
     * 控制器
     *
     * @param table
     */
    public void createController(SqlTableToBeanUtils.Table table, boolean enableSwagger,int type) {
        Map<String, Object> map = new HashMap<>();
        map.put("enableSwagger", enableSwagger);
        map.put("type",type);
        Template template = freeMarkerUtils.getTemplate("Controller.ftl");
        create(exportPath + "/" + javaPath + "/" + packagePath + "/controller", table.getEntityName() + "Controller.java", template, table, map);
    }

    /**
     * mybatisMapper.xml
     *
     * @param table
     */
    public void createMybatisMapper(SqlTableToBeanUtils.Table table, boolean enableMybatisPlus) {
        Map<String, Object> map = new HashMap<>();
        map.put("enableMybatisPlus", enableMybatisPlus);
        Template template = freeMarkerUtils.getTemplate("MybatisMapper.ftl");
        create(exportPath + "/" + resourcePath + "/mapper", table.getEntityName() + "Mapper.xml", template, table, map);
    }

    /**
     * mybatisMapper.java
     *
     * @param table
     */
    public void createMybatisMapperJava(SqlTableToBeanUtils.Table table, boolean enableMybatisPlus) {
        Map<String, Object> map = new HashMap<>();
        map.put("enableMybatisPlus", enableMybatisPlus);
        Template template = freeMarkerUtils.getTemplate("MybatisMapperJava.ftl");
        create(exportPath + "/" + javaPath + "/" + packagePath + "/mapper", table.getEntityName() + "Mapper.java", template, table, map);
    }


    private void create(String filePath, String fileName, Template template, SqlTableToBeanUtils.Table table, Map<String, Object> map) {
        //Map<String,Object> map = new HashMap<>();
        map.put("packageName", packageName);
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


}
