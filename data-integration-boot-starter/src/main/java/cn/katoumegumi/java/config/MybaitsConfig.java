package cn.katoumegumi.java.config;

import cn.katoumegumi.java.mybatis.SpringBootVFS;
import cn.katoumegumi.java.properties.MybatisProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.AllArgsConstructor;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

//@Configuration
//@ConditionalOnBean(value = DataSource.class)
//@AutoConfigureAfter(value = DataSourceConfig.class)
//@ConditionalOnProperty(prefix = "ws.mybatis",value = "enable",havingValue = "true")
//@EnableConfigurationProperties(value = {MybatisProperties.class})
@AllArgsConstructor
public class MybaitsConfig {

    @javax.annotation.Resource
    private MybatisProperties mybatisProperties;



    private final Interceptor[] interceptors;

    private final TypeHandler[] typeHandlers;

    private final LanguageDriver[] languageDrivers;

    private final ResourceLoader resourceLoader;

    private final DatabaseIdProvider databaseIdProvider;


    private final ApplicationContext applicationContext;




    public org.apache.ibatis.session.Configuration configuration(){
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //使全局的映射器启用或禁用缓存。
        configuration.setCacheEnabled(true);
        //全局启用或禁用延迟加载。当禁用时，所有关联对象都会即时加载。
        configuration.setLazyLoadingEnabled(true);
        //当启用时，有延迟加载属性的对象在被调用时将会完全加载任意属性。否则，每种属性将会按需要加载。
        configuration.setAggressiveLazyLoading(false);
        //是否允许单条sql 返回多个数据集  (取决于驱动的兼容性) default:true
        configuration.setMultipleResultSetsEnabled(true);
        //是否可以使用列的别名 (取决于驱动的兼容性) default:true
        configuration.setUseColumnLabel(true);
        // 允许JDBC 生成主键。需要驱动器支持。如果设为了true，这个设置将强制使用被生成的主键，有一些驱动器不兼容不过仍然可以执行。  default:false
        configuration.setUseGeneratedKeys(true);
        //指定 MyBatis 如何自动映射 数据基表的列 NONE：不隐射　PARTIAL:部分  FULL:全部
        configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
        //这是默认的执行类型  （SIMPLE: 简单； REUSE: 执行器可能重复使用prepared statements语句；BATCH: 执行器可以重复执行语句和批量更新）
        //configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        //使用驼峰命名法转换字段。
        configuration.setMapUnderscoreToCamelCase(true);
        //设置本地缓存范围 session:就会有数据的共享  statement:语句范围 (这样就不会有数据的共享 ) defalut:session
        configuration.setLocalCacheScope(LocalCacheScope.SESSION);
        //设置但JDBC类型为空时,某些驱动程序 要指定值,default:OTHER，插入空值时不需要指定类型
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        LogFactory.useStdOutLogging();
        return configuration;
    }


    public MybatisConfiguration mybatisConfiguration(){
        MybatisConfiguration configuration = new MybatisConfiguration();
        //使全局的映射器启用或禁用缓存。
        configuration.setCacheEnabled(true);
        //全局启用或禁用延迟加载。当禁用时，所有关联对象都会即时加载。
        configuration.setLazyLoadingEnabled(true);
        //当启用时，有延迟加载属性的对象在被调用时将会完全加载任意属性。否则，每种属性将会按需要加载。
        configuration.setAggressiveLazyLoading(false);
        //是否允许单条sql 返回多个数据集  (取决于驱动的兼容性) default:true
        configuration.setMultipleResultSetsEnabled(true);
        //是否可以使用列的别名 (取决于驱动的兼容性) default:true
        configuration.setUseColumnLabel(true);
        // 允许JDBC 生成主键。需要驱动器支持。如果设为了true，这个设置将强制使用被生成的主键，有一些驱动器不兼容不过仍然可以执行。  default:false
        configuration.setUseGeneratedKeys(true);
        //指定 MyBatis 如何自动映射 数据基表的列 NONE：不隐射　PARTIAL:部分  FULL:全部
        configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
        //这是默认的执行类型  （SIMPLE: 简单； REUSE: 执行器可能重复使用prepared statements语句；BATCH: 执行器可以重复执行语句和批量更新）
        //configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
        //使用驼峰命名法转换字段。
        configuration.setMapUnderscoreToCamelCase(true);
        //设置本地缓存范围 session:就会有数据的共享  statement:语句范围 (这样就不会有数据的共享 ) defalut:session
        configuration.setLocalCacheScope(LocalCacheScope.SESSION);
        //设置但JDBC类型为空时,某些驱动程序 要指定值,default:OTHER，插入空值时不需要指定类型
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        LogFactory.useStdOutLogging();
        return configuration;
    }



    /*@Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource proxyPrimaryDataSource){
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        //PagingIntercepter pagingIntercepter = new PagingIntercepter();
        Properties properties = new Properties();
        properties.setProperty("dbType","MYSQL");
        //pagingIntercepter.setProperties(properties);
        //sqlSessionFactoryBean.setPlugins(new Interceptor[]{pagingIntercepter});
        sqlSessionFactoryBean.setDataSource(proxyPrimaryDataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBean.setConfiguration(configuration());
        sqlSessionFactoryBean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        //sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource resources[] = resolver.getResources(mybatisProperties.getMapperXmlLocations());
            sqlSessionFactoryBean.setMapperLocations(resources);
        }catch (Exception e){
            e.printStackTrace();
        }
        return sqlSessionFactoryBean;
    }*/

    @Bean(name = "sqlSessionFactory")
    @Primary
    public MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean(DataSource dataSource){
        //PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //paginationInterceptor.setLimit(-1);
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        sqlSessionFactoryBean.setConfiguration(mybatisConfiguration());
        //sqlSessionFactoryBean.setPlugins(paginationInterceptor);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource resources[] = resolver.getResources(mybatisProperties.getMapperXmlLocations());
            sqlSessionFactoryBean.setMapperLocations(resources);
        }catch (Exception e){
            e.printStackTrace();
        }
        return sqlSessionFactoryBean;
    }



    /*@Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier(value = "sqlSessionFactoryBean")SqlSessionFactoryBean sqlSessionFactoryBean){
        SqlSessionFactory sqlSessionFactory = null;
        try {
            sqlSessionFactory = sqlSessionFactoryBean.getObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        return sqlSessionFactory;

    }*/

    /*@Bean
    public MapperScannerConfigurer mapperScannerConfigurer(Environment environment){
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage(environment.getProperty("ws.mybatis.base-package"));
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;

    }*/


}