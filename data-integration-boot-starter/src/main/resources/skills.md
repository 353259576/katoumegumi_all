# MySearchList 用法

查询条件构造器，链式 API 构建 SQL。

## 1. 创建

```java
MySearchList.create()             // 不绑定实体
MySearchList.create(User.class)   // 绑定实体，启用 SFunction lambda
```

## 2. 条件运算符

```java
.eq(User::getUsername, "admin")
// → username = 'admin'

.ne(User::getDelFlag, 1)
// → del_flag != 1

.gt(User::getAge, 18)
// → age > 18

.gte(User::getAge, 18)
// → age >= 18

.lt(User::getAge, 60)
// → age < 60

.lte(User::getAge, 60)
// → age <= 60

.like(User::getUsername, "张")
// → username LIKE '%张%'

.between(User::getAge, 18, 60)
// → age BETWEEN 18 AND 60

.notBetween(User::getAge, 18, 60)
// → age NOT BETWEEN 18 AND 60

.isNull(User::getPhone)
// → phone IS NULL

.isNotNull(User::getEmail)
// → email IS NOT NULL

.in(User::getId, Arrays.asList(1, 2, 3))
// → id IN (1, 2, 3)

.nIn(User::getStatus, Arrays.asList(0, 1))
// → status NOT IN (0, 1)

.sql("DATE(create_time) = ?", "2025-01-01")
// → DATE(create_time) = '2025-01-01'
```

## 3. condition — 条件分支

```java
searchList
    .condition(name != null, m -> m.like(User::getName, name))
    .condition(status != null, m -> m.eq(User::getStatus, status));
// name != null 时: ... AND name LIKE '%?%'
// status != null 时: ... AND status = ?
```

## 4. and / or — 分组

```java
searchList.or(
    m -> m.eq(Role::getTenantId, 1),
    m -> m.eq(Role::getType, 2)
);
// → ... tenant_id = 1 OR type = 2

searchList.and(and -> and.or(
    m -> m.eq(User::getStatus, 1),
    m -> m.eq(User::getStatus, 2)
));
// → ... AND (status = 1 OR status = 2)
```

## 5. 分页与排序

```java
searchList.setSqlLimit(l -> l.setOffset(0).setSize(10));
// → LIMIT 0, 10

searchList.setSqlLimit(l -> l.setCurrent(1).setSize(10));
// → LIMIT 0, 10（自动换算 offset = (1-1)*10）

searchList.sortDesc(User::getCreateTime);
// → ORDER BY create_time DESC

searchList.sortAsc(User::getId);
// → ORDER BY id ASC

searchList.sortDesc("t", User::getId);
// → ORDER BY t.id DESC
```

## 6. EXISTS / NOT EXISTS

```java
searchList.exists(
    MySearchList.create(Order.class)
        .setAlias("o")
        .singleColumnName(Order::getId)
        .eqp("o", Order::getUserId, "u", User::getId)
);
// → EXISTS (SELECT o.id FROM order o WHERE o.user_id = u.id)

searchList.exists(
    "SELECT id FROM t WHERE t.aid = u.id AND status = ?", 1
);
// → EXISTS (SELECT id FROM t WHERE t.aid = u.id AND status = 1)

searchList.notExists(subSearchList);
// → NOT EXISTS (...)

searchList.notExists("SELECT id FROM t WHERE t.aid = u.id", null);
// → NOT EXISTS (SELECT id FROM t WHERE t.aid = u.id)
```

## 7. 字段间比较

比较两张表的列值（非字面量）：

```java
.eqp("a", A::getX, "b", B::getY)
// → a.x = b.y

.gtp("a", A::getX, "b", B::getY)
// → a.x > b.y

.gtep(...)   // → a.x >= b.y
.ltp(...)    // → a.x < b.y
.ltep(...)   // → a.x <= b.y
```

## 8. IN 子查询

```java
searchList.in(User::getId,
    MySearchList.create(Order.class)
        .singleColumnName(Order::getUserId)
        .eq(Order::getStatus, 1)
);
// → id IN (SELECT user_id FROM order WHERE status = 1)
```

## 9. JOIN

```java
searchList.leftJoin(OrderDetail.class,
    t -> t.setJoinEntityPath("od")
          .setAlias("od")
          .on(Order::getId, OrderDetail::getOrderId)
          .condition(m -> m.eq("od", OrderDetail::getDelFlag, 0))
);
// → LEFT JOIN order_detail od ON order.id = od.order_id AND od.del_flag = 0

// INNER JOIN / RIGHT JOIN 同理
searchList.innerJoin(T.class, t -> t.setJoinEntityPath("x").on(...));
// → INNER JOIN t x ON ...

// 多级 JOIN: 上级是 "od", 再关联其子表
searchList.leftJoin(Detail.class,
    t -> t.setMainEntityPath("od")
          .setJoinEntityPath("d")
          .on(OrderDetail::getId, Detail::getDetailId)
);
// → LEFT JOIN detail d ON od.id = d.detail_id
```

## 10. UPDATE

```java
jdbcUtils.update(
    MySearchList.create(User.class)
        .set(User::getName, "李四")
        .set(User::getStatus, 1)
        .eq(User::getId, 100)
);
// → UPDATE user SET name = '李四', status = 1 WHERE id = 100

// 原子算术
jdbcUtils.update(
    MySearchList.create(Account.class)
        .eq(Account::getId, 1)
        .add(Account::getBalance, 100)
        .subtract(Account::getFrozen, 50)
);
// → UPDATE account SET balance = balance + 100, frozen = frozen - 50 WHERE id = 1
// multiply() / divide() 同理
```

## 11. DELETE

```java
jdbcUtils.delete(
    MySearchList.create(User.class).eq(User::getId, 100)
);
// → DELETE FROM user WHERE id = 100
```

## 12. 表别名与单列

```java
searchList.setAlias("u");
// → SELECT ... FROM user u

searchList.singleColumnName(User::getId);
// → 子查询只返回单列: SELECT id FROM ...

searchList.singleColumnName("u", User::getId);
// → 带别名: SELECT u.id FROM ...
```

## 13. sqlEquation — 位运算等复杂表达式

```java
searchList.sqlEquation(e ->
    e.column(User::getFlag).and().value(1).notEqual().value(0)
);
// → (flag & 1) != 0
```

## 14. 执行方法

| API | 等价 SQL |
|-----|---------|
| `jdbcUtils.getTOne(list)` | `SELECT ... LIMIT 1` |
| `jdbcUtils.getListT(list)` | `SELECT ...` |
| `jdbcUtils.getTPage(list)` | `SELECT ... LIMIT ?, ?` + `SELECT COUNT(*)` |
| `jdbcUtils.update(list)` | `UPDATE ... SET ... WHERE ...` |
| `jdbcUtils.delete(list)` | `DELETE FROM ... WHERE ...` |
