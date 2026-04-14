# katoumegumi_all

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/java-11%2B-green.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-1.0.8-blue.svg)](https://central.sonatype.com/)

一个轻量级的 Java MySQL 持久层框架，可与 MyBatis 和 JPA 共存，使用纯 Java 代码编写 SQL 语句。

## 目录

- [核心特性](#核心特性)
- [模块说明](#模块说明)
- [架构概览](#架构概览)
- [快速开始](#快速开始)
  - [引入依赖](#1-引入依赖)
  - [配置YAML](#2-配置yaml)
  - [定义实体](#3-定义实体)
- [实体定义](#实体定义)
- [WsJdbcUtils API 参考](#wsjdbcutils-api-参考)
- [MySearchList 条件构造器](#mysearchlist-条件构造器)
  - [基本查询](#基本查询)
  - [常用条件](#常用条件)
  - [表字段比较](#表字段比较)
  - [排序](#排序)
  - [条件判断](#条件判断)
  - [AND / OR 组合](#and--or-组合)
  - [自定义SQL片段](#自定义sql片段)
- [表关联查询](#表关联查询)
  - [内联接 INNER JOIN](#内联接-inner-join)
  - [左连接 LEFT JOIN](#左连接-left-join)
  - [右连接 RIGHT JOIN](#右连接-right-join)
  - [多表关联](#多表关联)
  - [关联附加条件](#关联附加条件)
  - [子查询嵌套](#子查询嵌套)
- [增删改操作](#增删改操作)
  - [新增](#新增)
  - [更新](#更新)
  - [删除](#删除)
  - [保存或更新](#保存或更新)
- [多数据源切换](#多数据源切换)
- [事务支持](#事务支持)
- [SQL拦截器（自动填充）](#sql拦截器自动填充)
- [代码生成器](#代码生成器)
- [Excel工具](#excel工具)
- [通用工具类](#通用工具类)
- [构建与发布](#构建与发布)
- [许可证](#许可证)

---

## 核心特性

- **低侵入**：基于 Spring JDBC Template 封装，与现有项目无缝集成
- **链式API**：使用 MySearchList 条件构造器，支持流式编程
- **多表关联**：支持 LEFT / RIGHT / INNER JOIN，复杂关联查询
- **自动填充**：支持查询 / 新增 / 修改拦截器，自动填充公共字段
- **多数据源**：支持多数据库配置和动态切换（基于 Druid 连接池）
- **双注解兼容**：同时支持 JPA（Jakarta Persistence）和 MyBatis-Plus 注解
- **代码生成**：内置代码生成器，可快速生成 Entity / Service / Controller / Mapper
- **Excel导出**：基于 POI 的流式 Excel 生成器，支持大数据量导出

---

## 模块说明

| 模块 | 说明 |
|------|------|
| data-integration-boot-starter | Spring Boot Starter，自动配置 WsJdbcUtils，入口类 |
| sql_utils | SQL 语句生成工具（支持 MySQL），包含条件构造器和拦截器 |
| common_utils | 通用工具类，反射、集合、字符串、Bean 转换等 |
| code_generator | 代码生成器，基于 FreeMarker 模板引擎 |
| excel_utils | Excel 操作工具，基于 Apache POI 的流式生成器 |

---

## 架构概览

```
┌─────────────────────────────────────────────────┐
│           data-integration-boot-starter          │
│  ┌──────────────┐  ┌──────────────────────────┐ │
│  │  WsJdbcUtils │  │  DataSourceConfig        │ │
│  │  (核心入口)   │  │  (多数据源自动配置)       │ │
│  └──────┬───────┘  └──────────────────────────┘ │
│         │                                        │
├─────────┼────────────────────────────────────────┤
│         ▼                                        │
│  ┌──────────────────────────────────────────┐    │
│  │             sql_utils                     │    │
│  │  ┌──────────────┐  ┌──────────────────┐  │    │
│  │  │MySearchList  │  │ SQLModelFactory  │  │    │
│  │  │(条件构造器)   │  │ (SQL模型工厂)     │  │    │
│  │  └──────────────┘  └──────────────────┘  │    │
│  │  ┌──────────────┐  ┌──────────────────┐  │    │
│  │  │SqlInterceptor│  │FieldColumnRelation│  │    │
│  │  │(SQL拦截器)    │  │MapperFactory     │  │    │
│  │  └──────────────┘  └──────────────────┘  │    │
│  └──────────────────────────────────────────┘    │
│                                                   │
├───────────────────────────────────────────────────┤
│             common_utils                          │
│  WsBeanUtils / WsReflectUtils / WsStringUtils    │
│  WsCollectionUtils / WsDateUtils / WsFileUtils   │
└───────────────────────────────────────────────────┘

  ┌─────────────────┐    ┌─────────────────┐
  │  code_generator │    │   excel_utils   │
  │  (代码生成器)    │    │  (Excel工具)     │
  └─────────────────┘    └─────────────────┘
```

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>cn.katoumegumi.java</groupId>
    <artifactId>data-integration-boot-starter</artifactId>
    <version>1.0.8.20250717</version>
</dependency>
```

### 2. 配置YAML

```yaml
megumi:
  datasource:
    enable: true
    druids:
      - alias: master
        url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
        username: root
        password: your_password
      - alias: slave1
        url: jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
        username: root
        password: your_password
```

### 3. 定义实体

```java
@Table(name = "ws_user")
@TableName(value = "ws_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "name")
    @TableField(value = "name")
    private String name;

    @Column(name = "age")
    @TableField(value = "age")
    private Integer age;

    // getter / setter ...
}
```

---

## 实体定义

框架同时支持 **JPA（Jakarta Persistence）** 和 **MyBatis-Plus** 注解，可按需选择或混合使用。

### JPA 注解

| 注解 | 说明 |
|------|------|
| `@Table(name = "xxx")` | 指定表名 |
| `@Column(name = "xxx")` | 指定列名 |
| `@Id` | 标记主键 |
| `@GeneratedValue` | 主键生成策略 |
| `@OneToMany` | 一对多关联 |
| `@JoinColumn` | 关联列配置 |

### MyBatis-Plus 注解

| 注解 | 说明 |
|------|------|
| `@TableName(value = "xxx")` | 指定表名 |
| `@TableField(value = "xxx")` | 指定列名 |
| `@TableId(type = IdType.AUTO)` | 标记主键及生成策略 |

### 关联对象定义

```java
@Entity
@Table(name = "ws_user_details")
@TableName(value = "ws_user_details")
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "user_id")
    @TableField(value = "user_id")
    private Long userId;

    @OneToMany
    @JoinColumn(name = "user_details_id", referencedColumnName = "id")
    private List<UserDetailsRemake> userDetailsRemake;

    // getter / setter ...
}
```

> 注解解析优先级：TableTemplate → Jakarta → Hibernate → MyBatis-Plus → Default（驼峰转下划线）

---

## WsJdbcUtils API 参考

`WsJdbcUtils` 是框架的核心入口类，通过 Spring Boot Starter 自动注入。

### 查询方法

| 方法 | 说明 |
|------|------|
| `getTOne(MySearchList)` | 查询单条记录 |
| `getTOne(T)` | 根据实体非空字段查询单条 |
| `getOne(Class<T>, Object...)` | 根据主键查询单条 |
| `getListT(MySearchList)` | 查询列表 |
| `getListT(T)` | 根据实体非空字段查询列表 |
| `getTPage(MySearchList)` | 分页查询（返回 MyBatis-Plus IPage） |
| `getTPage(T, IPage<?>)` | 根据实体非空字段分页查询 |
| `getCount(MySearchList)` | 统计数量 |

### 插入方法

| 方法 | 说明 |
|------|------|
| `insert(T)` | 新增单条，自动回填主键 |
| `insert(List<T>)` | 批量新增，自动回填主键 |

### 更新方法

| 方法 | 说明 |
|------|------|
| `update(T)` | 根据实体主键更新（仅更新非空字段） |
| `update(T, boolean)` | 根据实体主键更新，isAll=true 时更新所有字段 |
| `update(MySearchList)` | 根据条件更新 |
| `updateBatch(List<MySearchList>)` | 批量条件更新 |
| `updateBatchByT(List<T>)` | 批量实体更新 |
| `updateBatchByT(List<T>, boolean)` | 批量实体更新，isAll=true 时更新所有字段 |

### 删除方法

| 方法 | 说明 |
|------|------|
| `delete(MySearchList)` | 根据条件删除 |

### 其他方法

| 方法 | 说明 |
|------|------|
| `saveOrUpdate(T)` | 保存或更新（仅支持单主键） |
| `getJdbcTemplate()` | 获取底层 JdbcTemplate |

---

## MySearchList 条件构造器

`MySearchList` 是核心查询条件构造器，支持流式链式编程。

### 基本查询

```java
@Resource
private WsJdbcUtils jdbcUtils;

// 查询单条
MySearchList search = MySearchList.create(User.class)
    .eq(User::getId, 1);
User user = jdbcUtils.getTOne(search);

// 根据主键查询
User user = jdbcUtils.getOne(User.class, 1);

// 根据实体非空字段查询
User query = new User();
query.setName("张三");
List<User> list = jdbcUtils.getListT(query);

// 查询列表
List<User> list = jdbcUtils.getListT(search);

// 分页查询
MySearchList pageSearch = MySearchList.create(User.class)
    .setSqlLimit(limit -> limit.setCurrent(1).setSize(10));
IPage<User> page = jdbcUtils.getTPage(pageSearch);

// 统计数量
long count = jdbcUtils.getCount(search);
```

### 常用条件

```java
MySearchList search = MySearchList.create(User.class)
    // 等于 EQ
    .eq(User::getName, "张三")
    .ne(User::getName, "李四")         // 不等于

    // 比较运算
    .gt(User::getAge, 18)              // 大于
    .gte(User::getAge, 18)             // 大于等于
    .lt(User::getAge, 60)              // 小于
    .lte(User::getAge, 60)             // 小于等于

    // 模糊查询
    .like(User::getName, "张%")         // LIKE

    // IN / NOT IN
    .in(User::getId, Arrays.asList(1, 2, 3))
    .nIn(User::getStatus, Arrays.asList("deleted", "disabled"))

    // 空值判断
    .isNull(User::getDeleteTime)
    .isNotNull(User::getUpdateTime)

    // 范围查询
    .between(User::getAge, 18, 60)              // 闭区间
    .notBetween(User::getAge, 0, 17);           // 排除区间
```

### 表字段比较

```java
// 字段与字段比较（EQP = Equal to Parameter）
MySearchList search = MySearchList.create(User.class)
    .eqp(User::getName, User::getNickName)      // name = nickName
    .gtp(User::getAge, User::getMinAge)        // age > minAge
    .gtep(User::getAge, User::getMinAge)       // age >= minAge
    .ltp(User::getSalary, User::getMaxSalary)   // salary < maxSalary
    .ltep(User::getScore, User::getPassScore); // score <= passScore
```

### 排序

```java
MySearchList search = MySearchList.create(User.class)
    .sort(User::getCreateTime, "desc")          // 倒序
    .sort(User::getName, "asc")                 // 升序

    // 快捷方式
    .sortDesc(User::getCreateTime)
    .sortAsc(User::getName);
```

### 条件判断

```java
boolean isAdmin = true;

MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .condition(isAdmin, m -> m.eq(User::getRole, "admin"));  // 条件为true时才添加
```

### AND / OR 组合

```java
// AND 组合
MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .and(m -> m.gt(User::getAge, 18).lt(User::getAge, 60));

// OR 组合
MySearchList search = MySearchList.create(User.class)
    .or(m -> m.eq(User::getName, "张三"))
    .or(m -> m.eq(User::getName, "李四"));
```

### 自定义SQL片段

```java
MySearchList search = MySearchList.create(User.class)
    .sql("{u}.id = ?", Collections.singletonList(1))          // 自定义SQL
    .exists("select 1 from order o where o.user_id = {u}.id", Collections.singletonList(1))
    .notExists("select 1 from black_list b where b.user_id = {u}.id", Collections.singletonList(1));
```

---

## 表关联查询

### 内联接 INNER JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .innerJoin(UserDetails.class, t -> t
        .setJoinEntityPath(User::getDetails)       // 关联实体属性
        .setAlias("ud")                            // 关联表别名
        .on(User::getId, UserDetails::getUserId)  // ON条件
    );

List<User> list = jdbcUtils.getListT(search);
```

### 左连接 LEFT JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .leftJoin(Order.class, t -> t
        .setJoinEntityPath(User::getOrders)
        .setAlias("o")
        .on(User::getId, Order::getUserId)
    );
```

### 右连接 RIGHT JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .rightJoin(Department.class, t -> t
        .setJoinEntityPath(User::getDept)
        .setAlias("d")
        .on(User::getDeptId, Department::getId)
    );
```

### 多表关联

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // 第一层关联
    .innerJoin(UserDetails.class, t -> t
        .setJoinEntityPath(User::getDetails)
        .setAlias("ud")
        .on(User::getId, UserDetails::getUserId)
    )
    // 第二层关联（在已关联的表上继续关联）
    .innerJoin(Order.class, t -> t
        .setMainEntityPath("ud")                   // 指定已有表别名
        .setJoinEntityPath(UserDetails::getOrders)
        .setAlias("o")
        .on(UserDetails::getId, Order::getDetailsId)
    );
```

### 关联附加条件

```java
MySearchList search = MySearchList.create(User.class)
    .innerJoin(Order.class, t -> t
        .setJoinEntityPath(User::getOrders)
        .setAlias("o")
        .on(User::getId, Order::getUserId)
        .condition(m -> m.eq(Order::getStatus, "paid"))  // 附加ON条件
    );
```

### 子查询嵌套

```java
MySearchList search = MySearchList.create(User.class)
    // IN 子查询
    .in("details", UserDetails::getId,
        MySearchList.create(Order.class)
            .singleColumnName(Order::getDetailsId)  // 只返回单列
            .eq(Order::getStatus, "paid")
    );
```

---

## 增删改操作

### 新增

```java
// 新增单条（自动回填自增主键）
User user = new User();
user.setName("张三");
user.setAge(25);
jdbcUtils.insert(user);
// user.getId() 已自动回填

// 批量新增
List<User> users = Arrays.asList(user1, user2, user3);
jdbcUtils.insert(users);
```

### 更新

```java
// 根据实体主键更新（仅更新非空字段）
User user = new User();
user.setId(1L);
user.setName("李四");
jdbcUtils.update(user);

// 根据实体主键更新（更新所有字段，包括null）
jdbcUtils.update(user, true);

// 根据条件更新 - 使用SET指定更新字段
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1)
    .set(User::getName, "王五")
    .set(User::getAge, 30);
jdbcUtils.update(updateSearch);

// 字段自增/自减
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1)
    .add(User::getVisitCount, 1)       // visitCount = visitCount + 1
    .subtract(User::getBalance, 100);  // balance = balance - 100
jdbcUtils.update(updateSearch);

// 批量条件更新
List<MySearchList> updateList = new ArrayList<>();
updateList.add(MySearchList.create(User.class).eq(User::getId, 1).set(User::getName, "A"));
updateList.add(MySearchList.create(User.class).eq(User::getId, 2).set(User::getName, "B"));
jdbcUtils.updateBatch(updateList);

// 批量实体更新
List<User> users = Arrays.asList(user1, user2);
jdbcUtils.updateBatchByT(users);
```

### 删除

```java
// 根据主键删除
jdbcUtils.delete(MySearchList.create(User.class).eq(User::getId, 1));

// 根据多个主键删除
jdbcUtils.delete(MySearchList.create(User.class).in(User::getId, Arrays.asList(1, 2, 3)));

// 条件删除
MySearchList deleteSearch = MySearchList.create(User.class)
    .eq(User::getStatus, "deleted");
jdbcUtils.delete(deleteSearch);
```

### 保存或更新

```java
// 根据主键判断：主键为空则新增，主键不为空则查询是否存在再决定新增或更新
User user = new User();
user.setName("张三");
jdbcUtils.saveOrUpdate(user);  // id为空 → insert

user.setId(1L);
jdbcUtils.saveOrUpdate(user);  // id不为空 → 查询后 insert 或 update
```

> 注意：`saveOrUpdate` 仅支持单主键场景。

---

## 多数据源切换

框架基于 Druid 连接池和 Spring `AbstractRoutingDataSource` 实现多数据源动态切换。

### @DataBase 注解切换

```java
// 使用 @DataBase 注解切换数据源
@DataBase(dataBaseName = "slave1")
public List<User> getUsersFromSlave() {
    return jdbcUtils.getListT(MySearchList.create(User.class));
}
```

### 手动切换

```java
// 手动切换数据源
DynamicDataSourceHolder.setDataSource("slave1");
try {
    return jdbcUtils.getListT(search);
} finally {
    DynamicDataSourceHolder.clearDataSource();
}
```

> 注意：`@DataBase` 注解基于 AOP 实现，仅对 Spring 代理方法生效。手动切换需在 finally 中清理，避免线程污染。

---

## 事务支持

框架自动配置了 `DataSourceTransactionManager` 并启用了 `@EnableTransactionManagement`，可直接使用 Spring 事务注解。

### @Transactional 注解

```java
@Transactional
public void createUser(User user) {
    jdbcUtils.insert(user);
    // 异常时自动回滚
}
```

### WsTransactionUtils 编程式事务

```java
@Resource
private WsTransactionUtils transactionUtils;

public void createUser(User user) {
    transactionUtils.runMethod(() -> {
        jdbcUtils.insert(user);
        // 其他操作...
        return null;
    });
}
```

> `WsTransactionUtils` 默认隔离级别为 `READ_COMMITTED`，传播行为为 `REQUIRED`。

---

## SQL拦截器（自动填充）

SQL 拦截器用于在 INSERT / UPDATE / SELECT 操作时自动填充字段值（如创建时间、更新时间等）。

### 拦截器接口层次

```
BaseSqlInterceptor          (基础接口，定义 fieldName() 和 useCondition())
├── BaseInsertSqlInterceptor (插入拦截，定义 insertFill())
├── BaseUpdateSqlInterceptor (更新拦截，定义 updateFill())
└── BaseSelectSqlInterceptor (查询拦截，定义 selectFill())

AbstractSqlInterceptor      (组合接口，同时实现以上三个)
```

### 创建拦截器

```java
// 创建时间拦截器 - 仅在 INSERT 时自动填充
public class CreateTimeInterceptor implements BaseInsertSqlInterceptor {

    @Override
    public boolean isInsert() {
        return true;
    }

    @Override
    public Object insertFill() {
        return LocalDateTime.now();
    }

    @Override
    public String fieldName() {
        return "createTime";
    }
}

// 更新时间拦截器 - 仅在 UPDATE 时自动填充
public class UpdateTimeInterceptor implements BaseUpdateSqlInterceptor {

    @Override
    public boolean isUpdate() {
        return true;
    }

    @Override
    public Object updateFill() {
        return LocalDateTime.now();
    }

    @Override
    public String fieldName() {
        return "updateTime";
    }
}

// 组合拦截器 - 同时支持 INSERT 和 UPDATE
public class TimeInterceptor implements AbstractSqlInterceptor {

    @Override
    public boolean isInsert() {
        return true;
    }

    @Override
    public boolean isUpdate() {
        return true;
    }

    @Override
    public Object insertFill() {
        return LocalDateTime.now();
    }

    @Override
    public Object updateFill() {
        return LocalDateTime.now();
    }

    @Override
    public String fieldName() {
        return "updateTime";
    }
}
```

### 注册拦截器

```java
SQLModelFactory.addSqlInterceptor(new CreateTimeInterceptor());
SQLModelFactory.addSqlInterceptor(new UpdateTimeInterceptor());
```

### 拦截器接口说明

| 接口 | 方法 | 说明 |
|------|------|------|
| `BaseSqlInterceptor` | `fieldName()` | 需要自动注入的属性名称 |
| `BaseSqlInterceptor` | `useCondition(PropertyColumnRelationMapper)` | 是否对该实体生效（默认 true） |
| `BaseInsertSqlInterceptor` | `insertFill()` | 新增时填充的值 |
| `BaseUpdateSqlInterceptor` | `updateFill()` | 更新时填充的值 |
| `BaseSelectSqlInterceptor` | `selectFill()` | 查询时填充的值 |

---

## 代码生成器

`code_generator` 模块基于 FreeMarker 模板引擎，可从数据库表结构快速生成代码。

### 引入依赖

```xml
<dependency>
    <groupId>cn.katoumegumi.java</groupId>
    <artifactId>code_generator</artifactId>
    <version>1.0.8.20250717</version>
</dependency>
```

### 使用示例

```java
// 1. 创建数据源
DataSource dataSource = HikariCPDataSourceFactory.getDataSource(
    "jdbc:mysql://localhost:3306/your_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8",
    "root", "password", "com.mysql.cj.jdbc.Driver"
);

// 2. 创建生成器
Generator generator = new Generator("com.example.project", "/path/to/project");
generator.setEntityPath("entity/model")
    .setServicePath("service")
    .setServiceImplPath("service/impl")
    .setControllerPath("controller")
    .setEnableMybatisPlus(true)    // 启用 MyBatis-Plus 注解
    .setEnableHibernate(true)      // 启用 JPA 注解
    .setEnableSwagger(false)       // 关闭 Swagger
    .setEnableSpringDoc(true)      // 启用 SpringDoc
    .setEnableSearchVO(true);      // 生成查询VO

// 3. 获取表结构
SqlTableToBeanUtils sqlTableToBeanUtils = new SqlTableToBeanUtils(dataSource, "your_db", null);
List<SqlTableToBeanUtils.Table> tableList = sqlTableToBeanUtils.selectTables("table_name");

// 4. 生成代码
for (SqlTableToBeanUtils.Table table : tableList) {
    generator.createEntity(table);          // 实体类
    generator.createSearchVO(table);        // 查询VO
    generator.createService(table);         // 服务接口
    generator.createServiceImpl(table);     // 服务实现
    generator.createController(table);      // 控制器
    generator.createMybatisMapper(table);   // Mapper XML
    generator.createMybatisMapperJava(table); // Mapper Java
}
```

### 生成器配置项

| 方法 | 说明 | 默认值 |
|------|------|--------|
| `setEntityPath(String)` | 实体类相对路径 | `/entity` |
| `setServicePath(String)` | 服务接口相对路径 | `/service` |
| `setServiceImplPath(String)` | 服务实现相对路径 | `/service/impl` |
| `setControllerPath(String)` | 控制器相对路径 | `/controller` |
| `setJavaMapperPath(String)` | Mapper Java 相对路径 | `/mapper` |
| `setXmlMapperPath(String)` | Mapper XML 相对路径 | `/mapper` |
| `setSearchVOPath(String)` | 查询VO相对路径 | `/vo/search` |
| `setEnableMybatisPlus(Boolean)` | 启用 MyBatis-Plus 注解 | `true` |
| `setEnableHibernate(Boolean)` | 启用 JPA 注解 | `true` |
| `setEnableSwagger(Boolean)` | 启用 Swagger | `false` |
| `setEnableSpringDoc(Boolean)` | 启用 SpringDoc | `true` |
| `setEnableSearchVO(Boolean)` | 生成查询VO | `true` |
| `setEnableEntity(Boolean)` | 生成实体类 | `true` |
| `setEnableService(Boolean)` | 生成服务接口 | `true` |
| `setEnableController(Boolean)` | 生成控制器 | `true` |
| `setEnableMybatis(Boolean)` | 生成 MyBatis Mapper | `true` |
| `setType(Integer)` | 生成类型：0-sqlUtils 1-mybatisPlus 2-mybatis | `0` |

---

## Excel工具

`excel_utils` 模块基于 Apache POI 的 `SXSSFWorkbook`（流式写入），支持大数据量 Excel 导出。

### 引入依赖

```xml
<dependency>
    <groupId>cn.katoumegumi.java</groupId>
    <artifactId>excel_utils</artifactId>
    <version>1.0.8.20250717</version>
</dependency>
```

### 使用示例

```java
List<String[]> dataList = new ArrayList<>();
dataList.add(new String[]{"张三", "男", "25"});
dataList.add(new String[]{"李四", "女", "30"});

// 表头样式
ExcelTableHeadCellFill headStyle = location -> {
    CellStyle cellStyle = location.getCellStyle();
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
};

// 创建生成器
ExcelGenerator<String[]> generator = ExcelGenerator.create(dataList)
    .setTitle("用户列表")
    .addColumnProperty(c -> c
        .setColumnName("姓名")
        .setColumnWidthCellSize(2)
        .setExcelTableBodyCellFill((location, data) -> location.getCell().setCellValue(data[0]))
        .setExcelTableHeadCellFill(headStyle)
    )
    .addColumnProperty(c -> c
        .setColumnName("性别")
        .setColumnWidthCellSize(2)
        .setExcelTableBodyCellFill((location, data) -> location.getCell().setCellValue(data[1]))
        .setExcelTableHeadCellFill(headStyle)
    )
    .addColumnProperty(c -> c
        .setColumnName("年龄")
        .setColumnWidthCellSize(2)
        .setExcelTableBodyCellFill((location, data) -> location.getCell().setCellValue(data[2]))
        .setExcelTableHeadCellFill(headStyle)
    );

// 生成字节数组
byte[] bytes = generator.build();

// 写入文件
Files.write(Paths.get("output.xlsx"), bytes);
```

### 列配置项

| 方法 | 说明 |
|------|------|
| `setColumnName(String)` | 列标题 |
| `setColumnWidth(Integer)` | 列宽 |
| `setColumnHeight(Short)` | 列高 |
| `setColumnWidthCellSize(Integer)` | 横向合并单元格数 |
| `setColumnHeightCellSize(Integer)` | 纵向合并单元格数 |
| `setExcelTableHeadCellFill(ExcelTableHeadCellFill)` | 表头单元格样式 |
| `setExcelTableBodyCellFill(ExcelTableBodyCellFill<T>)` | 表体单元格填充 |
| `setExcelTableFootCellFill(ExcelTableFootCellFill)` | 表尾单元格样式 |

### ExcelPointLocation API

在单元格填充回调中，可通过 `ExcelPointLocation` 获取完整的 POI 对象：

| 属性 | 说明 |
|------|------|
| `getCell()` | 当前单元格 |
| `getRow()` | 当前行 |
| `getSheet()` | 当前 Sheet |
| `getWorkbook()` | 当前 Workbook |
| `getCellStyle()` | 单元格样式（自动创建） |
| `getRowValue()` | 当前行数据 |
| `getGlobalValue()` | 全局共享数据 |

---

## 通用工具类

`common_utils` 模块提供了一系列通用工具类：

| 工具类 | 说明 |
|--------|------|
| `WsBeanUtils` | Bean 操作工具，类型转换、对象创建 |
| `WsReflectUtils` | 反射工具，字段获取、方法调用、Lambda 解析 |
| `WsStringUtils` | 字符串工具，Unicode 解码、拼接、判空 |
| `WsCollectionUtils` | 集合工具，判空、转换、去重 |
| `WsDateUtils` | 日期工具，格式化、转换 |
| `WsFileUtils` | 文件工具，文件创建、路径处理 |
| `WsStreamUtils` | 流处理工具 |
| `Encryption` | 加密工具 |
| `SFunction<T, R>` | Lambda 序列化函数接口，用于 Lambda 属性名解析 |

### 类型转换

`WsBeanUtils.baseTypeConvert(Object, Class<T>)` 支持所有 Java 基本类型及其包装类、`String`、`BigDecimal`、`Date`、`LocalDate`、`LocalDateTime` 之间的自动转换。

---

## 构建与发布

```bash
# 编译
mvn compile

# 打包
mvn package

# 安装到本地仓库
mvn install

# 发布到 Maven Central
mvn deploy
```

> 版本号通过根 pom.xml 的 `${revision}` 属性统一管理，使用 `flatten-maven-plugin` 处理。

---

## 许可证

Apache License 2.0 - See [LICENSE](LICENSE)
