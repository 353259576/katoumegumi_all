# AGENTS.md

## 项目概述

Java Maven 多模块项目，一个轻量级 MySQL 持久层框架（类似 MyBatis-Plus）。

## 模块结构

| 模块 | 说明 |
|------|------|
| data-integration-boot-starter | 核心模块，Spring Boot Starter，入口类 `WsJdbcUtils` |
| sql_utils | SQL 语句生成工具 |
| common_utils | 通用工具类 |
| code_generator | 代码生成器 |
| excel_utils | Excel 操作工具 (POI) |

## 构建命令

```bash
mvn compile          # 编译
mvn package          # 打包
mvn install          # 安装到本地仓库
mvn deploy           # 发布到 Maven Central
```

## 重要说明

- **Java 版本**：11
- **Spring Boot 版本**：3.5.7
- **Maven 版本管理**：使用 `flatten-maven-plugin` 管理版本，版本号定义在根 pom.xml 的 `${revision}` 属性
- **仓库**：阿里云镜像中央仓库 + Sonatype Snapshots
- **License**：Apache 2.0