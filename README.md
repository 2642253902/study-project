# SpringBoot3 + Vue3 前后端分离项目模板

这是一个基于 **SpringBoot 3** 和 **Vue 3** 开发的前后端分离项目模板，采用 **Session** 验证方案，并集成了 **Redis** 缓存与 **邮件服务**。项目结构清晰，代码注释详尽，非常适合作为中小型项目的开发脚手架。

## 🌟 核心特性

*   **精美 UI 设计**：基于 **Element Plus** 组件库，定制化的响应式布局，适配多种屏幕尺寸。
*   **全流程认证**：
    *   **登录**：支持用户名/邮箱双重验证，内置强密码加密（BCrypt）。
    *   **注册**：集成邮箱验证码校验，防止恶意注册。
    *   **重置密码**：通过邮件发送 6 位数字验证码，安全可靠。
    *   **记住我**：基于 Spring Security 持久化令牌方案，实现跨会话持久登录。
*   **高可用架构**：
    *   **安全防御**：后端接口通过认证与权限过滤链（Security Filter Chain）保护。
    *   **状态同步**：前端 Pinia 状态管理与后端会话实时同步。
    *   **响应式数据校验**：使用 `hibernate-validator` 实现严格的入参校验。
    *   **Redis 缓存**：管理验证码生命周期，支持频率限制（3分钟有效，2分钟内禁止重复请求）。

## 🛠️ 技术选型

### 后端核心
| 技术                  | 说明                                |
| :-------------------- | :---------------------------------- |
| **Java 17**           | 长期支持版，使用最新语法特性        |
| **Spring Boot 3.5.1** | 核心框架，简化配置与部署            |
| **Spring Security 6** | 负责身份认证与权限授权              |
| **MyBatis 3**         | 持久层框架，配合 MySQL 实现数据存储 |
| **Redis**             | 缓存验证码，提高访问性能与安全性    |
| **Fastjson2**         | 高性能 JSON 序列化工具              |

### 前端核心
| 技术             | 说明                                 |
| :--------------- | :----------------------------------- |
| **Vue 3**        | Composition API 定义高效逻辑         |
| **Vite 6**       | 极速前端构建工具                     |
| **Pinia**        | 轻量级状态管理                       |
| **Axios**        | 标准异步请求库，深度封装支持凭证跨域 |
| **Element Plus** | 企业级 UI 组件库                     |

## 📂 详细目录结构

### 后端 (`study-project-barkend`)
- `com.exampe.config`: 配置中心（Security 权限配置、Web 跨域设置）。
- `com.exampe.controller`: RESTful 接口（认证功能 `AuthorizeController`、用户数据 `UserController`）。
- `com.exampe.entity`: 数据模型（包含统一响应实体 `RestBean`）。
- `com.exampe.service`: 业务逻辑层（验证码生成、邮件发送、注册/重置逻辑）。
- `db/`: 包含数据库初始化脚本 `study.sql`。

### 前端 (`study-project-fronrend`)
- `src/net`: 封装 Axios 的 `get`/`post` 请求，预处理业务错误。
- `src/router`: 包含权限守卫（Login Guard）的路由配置。
- `src/stores`: Pinia 存储用户授权信息。
- `src/components/welcome`: 登录、注册、找回密码等功能组件。

## 🔐 核心业务逻辑说明

### 1. 认证流程
系统采用传统的 **Session-Cookie** 模式。用户登录成功后，后端会在浏览器设置 `JSESSIONID`。前端 `axios` 请求已全局配置 `withCredentials: true`，确保每次请求都会自动携带 Cookie。

### 2. 路由守卫 (router/index.ts)
项目实现了完备的前端访问控制：
*   **受保护路径**：访问 `/index` 下的页面必须处于登录状态，否则强制跳转至登录页。
*   **反向重定向**：已登录用户无法再次访问登录/注册页面，将自动跳回主页。
*   **404 解析**：若访问不存在的页面，根据登录状态自动回退至首页或登录页。

### 3. 验证码机制
*   生成的验证码存储在 Redis 中，Key 关联了 Session ID 和邮箱，防止混淆。
*   设置了 **120秒** 内禁止重复发送的保护机制。

## 🚀 部署指南

### 环境需求
- JDK 17+
- MySQL 5.7+ (推荐 8.0)
- Redis 6+
- Node.js 20+

### 启动步骤
1.  **数据库**：执行 `db/study.sql`。
2.  **配置**：修改 `application.yaml` 中的 MySQL 连接、Redis 地址及邮件 SMTP 授权码。
3.  **运行后端**：启动 `StudyProjectBarkendApplication`。
4.  **运行前端**：
    ```bash
    cd study-project-fronrend
    npm install
    npm run dev
    ```

## 📝 开发者备注
*   邮件发送功能使用的是 Spring Boot Starter Mail，部署前请确保邮件协议及授权码填写正确。
*   本项目采用了 `form-login` 模式但返回 JSON 结果，方便前后端分离交互。