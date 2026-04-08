# katoumegumi_all

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/java-11%2B-green.svg)](https://openjdk.org/)

## 项目介绍

这是一个轻量级的Java MySQL持久层框架，可与MyBatis和JPA共存，使用纯Java代码编写SQL语句。

### 核心特性

- **低侵入**：基于Spring JDBC Template封装，与现有项目无缝集成
- **链式API**：使用MySearchList条件构造器，支持流式编程
- **多表关联**：支持LEFT/RIGHT/INNER JOIN，复杂关联查询
- **自动填充**：支持查询/新增/修改拦截器，自动填充公共字段
- **多数据源**：支持多数据库配置和动态切换

### 模块说明

| 模块 | 说明 |
|------|------|
| data-integration-boot-starter | Spring Boot Starter，自动配置WsJdbcUtils |
| common_utils | 通用工具类 |
| sql_utils | SQL语句生成工具（支持MySQL） |
| code_generator | 代码生成器（支持MyBatis/MyBatis Plus） |
| excel_utils | Excel操作工具（POI） |
| vertx-mysql-client-support | Vertx MySQL客户端支持 |

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
User user = jdbcUtils.getOne(search);

// 查询列表
List<User> list = jdbcUtils.getList(search);

// 分页查询
MySearchList pageSearch = MySearchList.create(User.class)
    .setSqlLimit(limit -> limit.setOffset(0).setSize(10));
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

List<User> list = jdbcUtils.getList(search);
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
// 新增单条
User user = new User();
user.setName("张三");
user.setAge(25);
jdbcUtils.save(user);

// 新增并返回ID
Long id = jdbcUtils.saveAndGetId(user);

// 批量新增
List<User> users = Arrays.asList(user1, user2, user3);
jdbcUtils.batchSave(users);
```

### 更新

```java
// 根据条件更新
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1);

User updateUser = new User();
updateUser.setName("李四");
jdbcUtils.update(updateSearch, updateUser);

// 使用SET指定更新字段
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1)
    .set(User::getName, "王五")
    .set(User::getAge, 30);
jdbcUtils.update(updateSearch, null);

// 字段自增/自减
MySearchList updateSearch = MySearchList.create(User.class)
    .eq(User::getId, 1)
    .add(User::getVisitCount, 1)       // visitCount = visitCount + 1
    .subtract(User::getBalance, 100); // balance = balance - 100
jdbcUtils.update(updateSearch, null);
```

### 删除

```java
// 根据ID删除
jdbcUtils.deleteById(User.class, 1);

// 批量删除
jdbcUtils.deleteByIds(User.class, Arrays.asList(1, 2, 3));

// 条件删除
MySearchList deleteSearch = MySearchList.create(User.class)
    .eq(User::getStatus, "deleted");
jdbcUtils.delete(deleteSearch);
```

---

## 多数据源切换

```java
// 使用@DataBase注解切换数据源
@DataBase(dataBaseName = "slave1")
public List<User> getUsersFromSlave() {
    return jdbcUtils.getList(MySearchList.create(User.class));
}

// 手动切换
DynamicDataSourceHolder.setDataSource("slave1");
try {
    return jdbcUtils.getList(search);
} finally {
    DynamicDataSourceHolder.clear();
}
```

---

## SQL拦截器（自动填充）

### 创建拦截器

```java
public class CreateTimeInterceptor implements AbstractSqlInterceptor {
    
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

public class UpdateTimeInterceptor implements AbstractSqlInterceptor {
    
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
```

### 注册拦截器

```java
SqlModelUtils.addSqlInterceptor(new CreateTimeInterceptor());
SqlModelUtils.addSqlInterceptor(new UpdateTimeInterceptor());
```

### 拦截器接口说明

| 方法 | 说明 |
|------|------|
| `isSelect()` | 是否在查询时生效 |
| `isInsert()` | 是否在新增时生效 |
| `isUpdate()` | 是否在更新时生效 |
| `insertFill()` | 新增时填充的值 |
| `updateFill()` | 更新时填充的值 |
| `selectFill()` | 查询时填充的值 |
| `fieldName()` | 作用的字段名 |

---

## 构建与发布

```bash
# 编译
mvn compile

# 打包
mvn package

# 安装到本地仓库
mvn install

# 发布到Maven Central
mvn deploy
```

---

## 许可证

Apache License 2.0 - See [LICENSE](LICENSE)