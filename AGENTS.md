# AGENTS.md

## 项目概述

Java 11 Maven 多模块项目，一个轻量级 MySQL 持久层框架（基于 Spring JDBC Template 封装，类似 MyBatis-Plus），groupId `cn.katoumegumi.java`，发布到 Maven Central。

- 同时支持 JPA（Jakarta Persistence）与 MyBatis-Plus 双注解，注解解析优先级：TableTemplate → Jakarta → Hibernate → MyBatis-Plus → 默认（驼峰转下划线）
- SQL 方言仅支持 MySQL
- 字段引用推荐用 `SFunction<T,R>` Lambda（如 `User::getId`），运行时解析为属性名

## 模块结构与依赖方向

| 模块 | 说明 | 依赖 |
|------|------|------|
| data-integration-boot-starter | Spring Boot Starter，入口类 `WsJdbcUtils`（`cn.katoumegumi.java.starter.jdbc.datasource`），多数据源（Druid + AbstractRoutingDataSource + `@DataBase` 注解切换） | sql_utils |
| sql_utils | 核心 SQL 生成引擎：`MySearchList` 条件构造器、`SQLModelFactory`、`SqlInterceptor` 自动填充拦截器（主体代码在此） | common_utils |
| common_utils | 基础工具：WsBeanUtils、WsReflectUtils、WsStringUtils、SFunction 等 | 无 |
| code_generator | FreeMarker 代码生成器（从数据库表生成实体/Service/Mapper） | sql_utils |
| excel_utils | POI SXSSF 流式 Excel 工具 | common_utils |

依赖方向：`common_utils ← sql_utils ← {data-integration-boot-starter, code_generator}`；`common_utils ← excel_utils`。

Spring Boot 自动配置入口：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` → `cn.katoumegumi.java.starter.jdbc.config.JdbcConfig`。

## 构建命令

```bash
mvn compile          # 编译（日常验证用这个）
mvn package          # 打包（会额外生成 sources 和 javadoc jar）
mvn install          # 安装到本地仓库
mvn deploy           # 发布到 Maven Central（GPG / central-publishing 插件当前在根 pom 中被注释）
```

**测试注意**：各模块 `src/test` 下是手工 `main` 方法的验证类（如 `cn.katoumegumi.java.sql.test.Test`），没有 JUnit 用例，`mvn test` 不执行任何有效断言。改动后请以 `mvn compile` 验证编译，必要时手动运行对应 main 方法。

## 重要说明

- **Java 版本**：11（由 `maven.compiler.release=11` 控制）；**Spring Boot 版本**：3.5.7
- **无 Lombok**：getter/setter 全部手写，不要引入 Lombok
- **版本管理**：所有模块版本由根 pom.xml 的 `${revision}` 属性统一管理（`flatten-maven-plugin` 处理），改版本只改 `revision` 一处
- **资源过滤**：根 pom 对 `src/main/resources` 开启了 `<filtering>true</filtering>`，资源文件中的 `${...}` 会被 Maven 替换
- **GraalVM Native**：boot-starter 内含 `META-INF/native-image/`（reflect-config.json、resource-config.json）和 `META-INF/spring/aot.factories`；框架大量依赖运行时反射读取实体字段，新增涉及反射的类/包时需同步更新这些配置
- **可选依赖**：大部分依赖声明为 `<optional>true</optional>`（由使用者按需引入），新增依赖时沿用该模式
- **编码**：全项目 UTF-8
- **License**：Apache 2.0

## 文档

- `README.md`：完整 API 文档（WsJdbcUtils 方法、MySearchList 条件构造器、JOIN/子查询、多数据源、事务、拦截器）。改动对外 API 时同步更新 README。