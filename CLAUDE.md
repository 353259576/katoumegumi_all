# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A lightweight Java MySQL persistence layer framework built on Spring JDBC Template, supporting both JPA (Jakarta Persistence) and MyBatis-Plus annotations. Published to Maven Central under `cn.katoumegumi.java`.

**Tech stack:** Java 11 (`--release` flag), Spring Boot 3.5.7, MyBatis-Plus 3.5.14, Apache POI 5.4.0, Druid 1.2.18, Freemarker 2.3.30, Vert.x JDBC client.

## Module Structure & Dependencies

| Module | Purpose | Depends On |
|--------|---------|------------|
| `data-integration-boot-starter` | Spring Boot Starter; auto-configures WsJdbcUtils, multi-datasource via Druid + AbstractRoutingDataSource | sql_utils, common_utils, druid, spring-boot |
| `sql_utils` | Core SQL generation engine: MySearchList fluent builder, SQLModelFactory, SqlHandler (MySQL), interceptors | common_utils, mybatis-plus-annotation, jakarta.persistence-api |
| `common_utils` | Utilities: WsBeanUtils, WsReflectUtils, WsStringUtils, WsCollectionUtils, ConvertUtils, SFunction<T,R> lambda interface | none |
| `code_generator` | FreeMarker-based code generator for Entity/Service/Controller/Mapper from DB schema | sql_utils, freemarker |
| `excel_utils` | Apache POI SXSSFWorkbook-based streaming Excel exporter | poi, poi-ooxml |

## Key Classes & Architecture

### Entry Point: `WsJdbcUtils` (boot-starter)
Core entry point for all database operations. Provides `insert()`, `update()`, `delete()`, `selectList()`, `selectOne()`, `selectPage()`, `saveOrUpdate()` etc. Each method accepts a `MySearchList` condition builder and returns typed results.

### SQL Builder: `MySearchList` (sql_utils)
Fluent condition builder — chain `.eq()`, `.gt()`, `.in()`, `.and()`, `.or()`, `.leftJoin()` etc. Uses `SFunction<T,R>` for type-safe lambda field references (`User::getStatus`). Also supports aggregate functions via `SqlFunction.create("COUNT", "id")`.

### SQL Generation: `SQLModelFactory` (sql_utils)
Builds SQL entities from MySearchList. Pluggable via `SqlHandler` interface — default implementation is `MysqlSqlHandler`. Factory also manages global SQL interceptors and field-column mapping strategies.

### Multi-Datasource: `DynamicDataSource` + `@DataBase`
Switches datasource at runtime via AOP. Annotate methods with `@DataBase(dataBaseName = "alias")` or use `DynamicDataSourceHolder` manually. Configured via Druid connection pools in `DataSourceConfig`.

### Lambda Support: `SFunction<T,R>` (common_utils)
Custom functional interface that serializes lambda references to property names at runtime, enabling type-safe column references without string magic.

## Entity Annotation Strategy

Entity classes can use both JPA and MyBatis-Plus annotations simultaneously. Resolution priority: TableTemplate → Jakarta Persistence → Hibernate → MyBatis-Plus → default (camelCase to snake_case).

```java
@Entity @Table(name = "ws_user")
@TableName(value = "ws_user")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
}
```

## SQL Interceptors

Register globally via `SQLModelFactory.addSqlInterceptor()`:
- **`BaseInsertSqlInterceptor`** — auto-fill insert fields (createTime, createUser)
- **`BaseUpdateSqlInterceptor`** — auto-fill update fields (updateTime, updateUser)
- **`AbstractSqlInterceptor`** — generic interceptor for any SQL phase

## Field-Column Mapping Strategies

Pluggable via `FieldColumnRelationMapperFactory`. Built-in strategies:
1. **TableTemplate** — custom `@TableTemplate` annotation
2. **Jakarta Persistence** — `@Column`, `@Entity`, `@Table`
3. **Hibernate** — `@Column` from Hibernate annotations
4. **MyBatis-Plus** — `@TableName`, `@TableId`, `@TableField`
5. **Default** — camelCase to snake_case conversion

## Spring Boot Auto-Configuration

Two auto-config classes registered in `META-INF/spring.factories`:
- **`DataSourceConfig`** — Configures Druid data sources and AbstractRoutingDataSource for multi-datasource support.
- **`JdbcConfig`** — Configures WsJdbcUtils bean and transaction manager.

YAML config property prefix: `megumi.datasource.druids[*]` with fields `alias`, `url`, `username`, `password`.

Transaction management via `WsTransactionUtils` in boot-starter.

## Build & Development

```bash
# Compile all modules
mvn compile

# Package (produces JARs with sources + javadoc)
mvn package

# Install to local repo
mvn install

# Deploy to Maven Central (requires GPG signing)
mvn deploy
```

Version managed via `${revision}` property in root pom.xml. Uses `flatten-maven-plugin` for runtime version resolution. Repositories configured with Alibaba mirror + Sonatype snapshots.

## Test Classes

Test classes exist under `data-integration-boot-starter/src/test/`:
- **Models:** `User`, `LUser`, `LUserDetail`, `UserDetails`, `UserDetailsRemake`, etc.
- **Tests:** `FieldTest.java`, `Test.java`

Run with:
```bash
mvn test -pl data-integration-boot-starter
```

## Important Notes

- All modules are optional dependencies (`<optional>true</optional>`) — consumers pick what they need.
- The GPG signing plugin points to Windows path `C://Program Files (x86)//GnuPG//bin//gpg.exe`.
- No Lombok used in this project.
- Chinese comments throughout the codebase; class names are English but some Javadoc is in Chinese.
- Uses Java module system (`module-info.java`) in each module.
- All modules use `--release 11` compilation flag (not source/target).
