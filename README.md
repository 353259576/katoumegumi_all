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
  - [比较运算](#比较运算)
  - [范围查询](#范围查询)
  - [空值判断](#空值判断)
  - [模糊查询](#模糊查询)
  - [IN查询](#in查询)
  - [表字段比较](#表字段比较)
  - [算术运算](#算术运算)
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
- [通用工具类](#通用工具类)
- [构建与发布](#构建与发布)
- [许可证](#许可证)

---

## 核心特性

- **低侵入**：基于 Spring JDBC Template 封装，与现有项目无缝集成
- **链式API**：使用 MySearchList 条件构造器，支持流式编程
- **类型安全**：支持 Lambda/SFunction 方式引用字段，编译期检查
- **多表关联**：支持 LEFT / RIGHT / INNER JOIN，复杂关联查询
- **自动填充**：支持查询 / 新增 / 修改拦截器，自动填充公共字段
- **多数据源**：支持多数据库配置和动态切换（基于 Druid 连接池）
- **双注解兼容**：同时支持 JPA（Jakarta Persistence）和 MyBatis-Plus 注解

---

## 模块说明

| 模块 | 说明 |
|------|------|
| data-integration-boot-starter | Spring Boot Starter，自动配置 WsJdbcUtils，入口类 |
| sql_utils | SQL 语句生成工具（支持 MySQL），包含条件构造器和拦截器 |
| common_utils | 通用工具类，反射、集合、字符串、Bean 转换等 |

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
    enable: true                    # 启用数据源自动配置
    seataEnable: false              # 是否启用 Seata 分布式事务
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

**强烈推荐使用 `Lambda/SFunction` 方式处理字段名**，示例中均使用 Lambda 表达式（如 `User::getId`），这样可以在编译期检查字段引用的正确性。同时也提供了 String 字段名的重载方法作为兼容。

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

### 比较运算

MySearchList 支持所有常见的比较运算符：

```java
MySearchList search = MySearchList.create(User.class)
    // 等于
    .eq(User::getName, "张三")

    // 不等于
    .ne(User::getName, "李四")

    // 大于
    .gt(User::getAge, 18)

    // 大于等于
    .gte(User::getAge, 18)

    // 小于
    .lt(User::getAge, 60)

    // 小于等于
    .lte(User::getAge, 60);

List<User> list = jdbcUtils.getListT(search);
```

**带表别名的比较运算**：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .eq("u", User::getName, "张三")       // u.name = '张三'
    .gt("u", User::getAge, 18);           // u.age > 18
```

### 范围查询

```java
MySearchList search = MySearchList.create(User.class)
    // 闭区间查询 [18, 60]
    .between(User::getAge, 18, 60)

    // 排除区间查询，等价于 age < 0 OR age > 17
    .notBetween(User::getAge, 0, 17);
```

### 空值判断

```java
MySearchList search = MySearchList.create(User.class)
    // 字段为NULL
    .isNull(User::getDeleteTime)

    // 字段不为NULL
    .isNotNull(User::getUpdateTime);
```

**注意**：当使用 `.eq(field, null)` 时，会自动转换为 `.isNull(field)`：

```java
// 以下两种写法等价
MySearchList.create(User.class).eq(User::getDeleteTime, null);
MySearchList.create(User.class).isNull(User::getDeleteTime);
```

### 模糊查询

```java
MySearchList search = MySearchList.create(User.class)
    // 模糊匹配，需要自己写通配符
    .like(User::getName, "%张%")          // 包含"张"
    .like(User::getName, "张%")           // 以"张"开头
    .like(User::getName, "%三");          // 以"三"结尾
```

### IN查询

```java
MySearchList search = MySearchList.create(User.class)
    // IN 查询
    .in(User::getId, Arrays.asList(1, 2, 3))
    .in(User::getStatus, Arrays.asList("active", "pending"))

    // NOT IN 查询
    .nIn(User::getStatus, Arrays.asList("deleted", "disabled"));
```

### 表字段比较

当需要比较两个字段的值时，使用 `eqp/gtp/gtep/ltp/ltep` 方法（p 代表 parameter）：

```java
MySearchList search = MySearchList.create(User.class)
    // 字段与字段比较
    .eqp(User::getName, User::getNickName)       // name = nickName
    .gtp(User::getAge, User::getMinAge)          // age > minAge
    .gtep(User::getAge, User::getMinAge)         // age >= minAge
    .ltp(User::getSalary, User::getMaxSalary)    // salary < maxSalary
    .ltep(User::getScore, User::getPassScore);   // score <= passScore
```

**带表别名的字段比较**：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // 指定两个字段的表别名
    .eqp("u", User::getName, "u", User::getNickName)   // u.name = u.nickName
    .gtp("u", User::getAge, "d", Department::getMinAge); // u.age > d.minAge
```

### 算术运算

算术运算主要用于 UPDATE 操作：

```java
// 字段自增/自减
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1)
    .add(User::getVisitCount, 1)            // visitCount = visitCount + 1
    .subtract(User::getBalance, 100)        // balance = balance - 100
    .multiply(User::getScore, 1.1)          // score = score * 1.1
    .divide(User::getTotal, 2);             // total = total / 2

jdbcUtils.update(updateSearch);
```

### 排序

```java
MySearchList search = MySearchList.create(User.class)
    // 指定排序方式
    .sort(User::getCreateTime, "desc")      // 倒序
    .sort(User::getName, "asc")             // 升序

    // 快捷方式
    .sortDesc(User::getCreateTime)          // 创建时间倒序
    .sortAsc(User::getName);                // 名称升序
```

**多字段排序**：

```java
MySearchList search = MySearchList.create(User.class)
    .sortDesc(User::getCreateTime)          // 首先按创建时间倒序
    .sortAsc(User::getName)                 // 然后按名称升序
    .sortDesc(User::getAge);                // 最后按年龄倒序
```

**带表别名的排序**：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .sortDesc("u", User::getCreateTime);    // ORDER BY u.create_time DESC
```

### 条件判断

当某些查询条件需要根据业务逻辑动态添加时，使用 `condition` 方法：

```java
boolean isAdmin = true;
String searchName = "张三";
Integer minAge = 18;

MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    // 条件为true时才添加条件
    .condition(isAdmin, m -> m.eq(User::getRole, "admin"))
    // 动态条件：只有当参数不为空时才添加
    .condition(StringUtils.isNotBlank(searchName), m -> m.like(User::getName, "%" + searchName + "%"))
    .condition(minAge != null, m -> m.gte(User::getAge, minAge));
```

### AND / OR 组合

**AND 组合**：默认就是 AND，也可以显式使用：

```java
// 默认就是 AND（等价于 status = 'active' AND age > 18 AND age < 60）
MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .lt(User::getAge, 60);

// 显式使用 and 组合嵌套条件
MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .and(m -> m.gt(User::getAge, 18).lt(User::getAge, 60));
// 等价于: status = 'active' AND (age > 18 AND age < 60)
```

**OR 组合**：

```java
// 简单的 OR 条件
MySearchList search = MySearchList.create(User.class)
    .or(m -> m.eq(User::getName, "张三"))
    .or(m -> m.eq(User::getName, "李四"));
// 等价于: name = '张三' OR name = '李四'

// 混合 AND 和 OR
MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .or(m -> m.eq(User::getRole, "admin"))
    .or(m -> m.gt(User::getScore, 100));
// 等价于: status = 'active' OR role = 'admin' OR score > 100
```

**复杂的 AND/OR 嵌套**：

```java
MySearchList search = MySearchList.create(User.class)
    .eq(User::getStatus, "active")
    .and(m -> m
        .or(n -> n.eq(User::getRole, "admin"))
        .or(n -> n.eq(User::getRole, "manager").gt(User::getLevel, 5))
    );
// 等价于: status = 'active' AND (role = 'admin' OR (role = 'manager' AND level > 5))
```

### 自定义SQL片段

当 MySearchList 提供的方法无法满足需求时，可以使用自定义 SQL：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // 自定义 SQL 片段，{u} 会被替换为表别名
    .sql("{u}.id = ?", Collections.singletonList(1))

    // EXISTS 子查询
    .exists("select 1 from orders o where o.user_id = {u}.id", Collections.singletonList(1))

    // NOT EXISTS 子查询
    .notExists("select 1 from black_list b where b.user_id = {u}.id", Collections.singletonList(1));
```

**使用 MySearchList 子查询**：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // 使用 MySearchList 构建 EXISTS 子查询
    .exists(
        MySearchList.create(Order.class)
            .setAlias("o")
            .eqp("o", Order::getUserId, "u", User::getId)
            .gt(Order::getAmount, 1000)
    );
```

**SqlEquation（链式表达式）**：

```java
MySearchList search = MySearchList.create(User.class)
    // 链式表达式：name + password = 1
    .sqlEquation(e -> e
        .column(User::getName)
        .add()
        .column(User::getPassword)
        .equal()
        .value(1)
    )
    // 链式表达式：name IN ('a', 'b')
    .sqlEquation(e -> e
        .column(User::getName)
        .in()
        .value(Arrays.asList("a", "b"))
    );
```

---

## 表关联查询

MySearchList 提供了强大的多表关联功能，支持 INNER JOIN、LEFT JOIN、RIGHT JOIN。

### 内联接 INNER JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .innerJoin(UserDetails.class, t -> t
        .setJoinEntityPath(User::getDetails)       // 关联实体属性（支持 SFunction）
        .setAlias("ud")                            // 关联表别名
        .on(User::getId, UserDetails::getUserId)   // ON条件（支持 SFunction）
    );

List<User> list = jdbcUtils.getListT(search);
// 生成SQL: SELECT ... FROM ws_user u INNER JOIN ws_user_details ud ON u.id = ud.user_id
```

### 左连接 LEFT JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .leftJoin(Order.class, t -> t
        .setAlias("o")
        .on(User::getId, Order::getUserId)
    );

List<User> list = jdbcUtils.getListT(search);
// 生成SQL: SELECT ... FROM ws_user u LEFT JOIN ws_order o ON u.id = o.user_id
```

### 右连接 RIGHT JOIN

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .rightJoin(Department.class, t -> t
        .setAlias("d")
        .on(User::getDeptId, Department::getId)
    );

List<User> list = jdbcUtils.getListT(search);
// 生成SQL: SELECT ... FROM ws_user u RIGHT JOIN ws_department d ON u.dept_id = d.id
```

### 多表关联

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // 第一层关联
    .innerJoin(UserDetails.class, t -> t
        .setAlias("ud")
        .on(User::getId, UserDetails::getUserId)
    )
    // 第二层关联（在已关联的表上继续关联）
    .innerJoin(Order.class, t -> t
        .setMainEntityPath("ud")                   // 指定主表为已关联的 ud
        .setAlias("o")
        .on(UserDetails::getId, Order::getDetailsId)
    );

// 生成SQL:
// SELECT ... FROM ws_user u
// INNER JOIN ws_user_details ud ON u.id = ud.user_id
// INNER JOIN ws_order o ON ud.id = o.details_id
```

### 关联附加条件

在 JOIN 的 ON 子句中添加额外条件：

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    .innerJoin(Order.class, t -> t
        .setAlias("o")
        .on(User::getId, Order::getUserId)
        // 在 ON 条件中添加额外条件
        .condition(m -> m
            .eq("o", Order::getStatus, "paid")
            .gt("o", Order::getAmount, 100)
        )
    );

// 生成SQL:
// SELECT ... FROM ws_user u
// INNER JOIN ws_order o ON u.id = o.user_id AND o.status = 'paid' AND o.amount > 100
```

### 子查询嵌套

```java
MySearchList search = MySearchList.create(User.class)
    .setAlias("u")
    // IN 子查询
    .in("u", "id",
        MySearchList.create(Order.class)
            .singleColumnName(Order::getUserId)   // 只返回单列
            .eq(Order::getStatus, "paid")
    );

// 生成SQL:
// SELECT ... FROM ws_user u WHERE u.id IN (SELECT user_id FROM ws_order WHERE status = 'paid')
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
    .add(User::getVisitCount, 1)            // visitCount = visitCount + 1
    .subtract(User::getBalance, 100);       // balance = balance - 100
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
├── BaseInsertSqlInterceptor (插入拦截，定义 isInsert() + insertFill())
├── BaseUpdateSqlInterceptor (更新拦截，定义 isUpdate() + updateFill())
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
| `BaseInsertSqlInterceptor` | `isInsert()` | 是否在 INSERT 时触发 |
| `BaseInsertSqlInterceptor` | `insertFill()` | 新增时填充的值 |
| `BaseUpdateSqlInterceptor` | `isUpdate()` | 是否在 UPDATE 时触发 |
| `BaseUpdateSqlInterceptor` | `updateFill()` | 更新时填充的值 |
| `BaseSelectSqlInterceptor` | `selectFill()` | 查询时填充的值 |

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
