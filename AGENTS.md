# Legado (开源阅读) - AI 助手工作上下文

> 本文档为 AI 助手提供项目的工作上下文，包含项目概述、技术栈、架构、API 接口和数据库 Schema 等关键信息。

---

## 项目概述

**项目名称**：Legado / 开源阅读  
**项目类型**：Android 平台免费开源小说阅读器  
**开源协议**：GPL-3.0  
**仓库地址**：https://github.com/gedoor/legado

### 主要功能

- 自定义书源，支持规则抓取网页数据（XPath、JSONPath、JS 脚本、正则表达式）
- 列表/网格书架自由切换，支持书籍分组管理
- RSS 订阅内容管理，支持文章收藏和阅读
- 本地书籍导入（TXT、EPUB、PDF、MOBI、AZW/AZW3）
- 高度自定义阅读界面（字体、颜色、背景、行距等）
- 多种翻页模式（覆盖、仿真、滑动、滚动）
- TTS 朗读和音频播放功能
- Web 服务和 Content Provider API 供外部调用

### 技术栈概览

| 类别 | 技术 | 版本 |
|------|------|------|
| **开发语言** | Kotlin | 2.3.0 |
| **Android SDK** | compileSdk / targetSdk | 36 |
| **最低支持** | minSdk | 21 |
| **JVM 版本** | Java | 17 |
| **构建工具** | Android Gradle Plugin | 8.13.2 |
| **数据库** | Room | 2.7.1 |
| **异步处理** | Kotlin Coroutines | 1.10.2 |
| **网络库** | OkHttp / Cronet | 5.3.2 / 128.0.6613.40 |
| **图像加载** | Glide | 5.0.5 |
| **数据解析** | Gson, Jsoup, JsonPath | - |
| **JS 引擎** | Rhino | 1.8.1 |
| **架构模式** | MVVM | - |

---

## 项目结构

```
legado/
├── app/                      # 主应用模块 (Kotlin + Android)
│   ├── src/main/
│   │   ├── java/io/legado/app/
│   │   │   ├── data/         # 数据层 (Room 数据库实体、DAO)
│   │   │   ├── model/        # 业务逻辑层 (规则解析、网络请求)
│   │   │   ├── ui/           # UI 层 (Activity、Fragment、ViewModel)
│   │   │   ├── service/      # 后台服务 (播放、下载、Web 服务)
│   │   │   ├── help/         # 配置和工具类
│   │   │   ├── api/          # 对外 API 控制器
│   │   │   └── web/          # 内置 Web 服务器
│   │   ├── res/              # 资源文件
│   │   └── AndroidManifest.xml
│   └── schemas/              # Room 数据库 Schema (版本 1-75)
│
├── modules/
│   ├── book/                 # 电子书格式解析库 (Java)
│   │   ├── epublib/          # EPUB 2.0/3.0 解析
│   │   └── umdlib/           # UMD 格式解析
│   │
│   ├── rhino/                # JavaScript 引擎模块 (Kotlin)
│   │   └── src/main/java/com/script/rhino/  # Rhino JS 引擎封装
│   │
│   └── web/                  # Web 前端模块 (Vue.js)
│       ├── src/              # Vue 3 + TypeScript + Element Plus
│       └── package.json      # 前端依赖配置
│
├── gradle/
│   ├── libs.versions.toml    # 依赖版本目录
│   └── wrapper/              # Gradle Wrapper
│
└── .github/workflows/        # CI/CD 工作流
```

### 模块说明

| 模块 | 职责 | 技术栈 |
|------|------|--------|
| **app** | 主应用模块，包含所有 Android 业务逻辑 | Kotlin, AndroidX, Room |
| **book** | 电子书格式解析（EPUB, UMD） | 纯 Java 库 |
| **rhino** | JavaScript 脚本引擎，用于书源规则执行 | Kotlin + Rhino |
| **web** | Web 管理界面，通过 HTTP/WebSocket 与 App 通信 | Vue 3, TypeScript, Element Plus |

---

## 构建和运行

### 环境要求

- JDK 17+
- Android SDK (API 36)
- Gradle 8.x (通过 Gradle Wrapper 自动下载)

### 核心命令

```bash
# 清理构建
./gradlew clean

# 构建项目
./gradlew build

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 安装到设备（需连接设备或启动模拟器）
./gradlew installDebug
```

### 构建变体

- **debug**: 未混淆，包名后缀 `.debug`
- **release**: 启用混淆和资源压缩，包名后缀 `.release`

---

## 架构说明

### MVVM 架构模式

```
View (Activity/Fragment)
    ↕ 数据绑定
ViewModel
    ↕ LiveData/Flow
Repository
    ↕ 
Data Source (Room DB + Network)
```

### 核心组件

**UI 层** (`ui/`)
- `MainActivity`: 主界面，书架管理
- `ReadBookActivity`: 阅读界面
- `BookSourceActivity`: 书源管理
- `RssSourceActivity`: RSS 源管理

**业务逻辑层** (`model/`)
- `analyzeRule/`: 规则解析引擎（XPath, JSONPath, JS, 正则）
- `webBook/`: 网络书籍获取
- `localBook/`: 本地书籍解析
- `ReadBook`: 阅读核心逻辑

**数据层** (`data/`)
- `AppDatabase`: Room 数据库（版本 75）
- `entities/`: 21 个数据实体
- `dao/`: 21 个数据访问对象

**服务层** (`service/`)
- `AudioPlayService`: 音频播放服务
- `CacheBookService`: 书籍缓存服务
- `DownloadService`: 下载服务
- `WebService`: Web API 服务（端口 1234/1235）

---

## API 接口文档

> 详细 API 文档请参考 [api.md](api.md)

### Web API（HTTP）

**基础地址**: `http://127.0.0.1:1234`（本地访问，远程访问请替换为手机 IP）

#### 书源管理 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/saveBookSource` | POST | 插入单个书源（JSON Body） |
| `/saveBookSources` | POST | 插入多个书源（JSON 数组） |
| `/getBookSource?url=xxx` | GET | 获取指定书源 |
| `/getBookSources` | GET | 获取所有书源 |
| `/deleteBookSources` | POST | 删除多个书源（JSON 数组） |

**请求格式**: 参考 `app/src/main/java/io/legado/app/data/entities/BookSource.kt`

#### RSS 源管理 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/saveRssSources` | POST | 插入多个 RSS 源（JSON 数组） |
| `/getRssSource?url=xxx` | GET | 获取指定 RSS 源 |
| `/getRssSources` | GET | 获取所有 RSS 源 |
| `/deleteRssSources` | POST | 删除多个 RSS 源（JSON 数组） |

#### 书籍管理 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/saveBook` | POST | 插入书籍 |
| `/deleteBook` | POST | 删除书籍 |
| `/getBookshelf` | GET | 获取所有书籍 |
| `/getChapterList?url=xxx` | GET | 获取章节列表 |
| `/getBookContent?url=xxx&index=1` | GET | 获取指定章节内容 |
| `/saveBookProgress` | POST | 保存阅读进度 |
| `/cover?path=xxxxx` | GET | 获取封面图片 |
| `/image?url=${bookUrl}&path=${picUrl}&width=${width}` | GET | 获取正文图片 |

**请求格式**: 参考 `app/src/main/java/io/legado/app/data/entities/Book.kt`

#### 替换规则 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/getReplaceRules` | GET | 获取所有替换规则 |
| `/saveReplaceRule` | POST | 插入替换规则 |
| `/deleteReplaceRule` | POST | 删除替换规则 |
| `/testReplaceRule` | POST | 测试替换规则 |

**请求格式**: 参考 `app/src/main/java/io/legado/app/data/entities/ReplaceRule.kt`

### WebSocket API

**基础地址**: `ws://127.0.0.1:1235`（WebSocket 端口）

| 端点 | 消息格式 | 说明 |
|------|---------|------|
| `/bookSourceDebug` | `{ key: String, tag: String }` | 书源调试（tag 为源链接） |
| `/rssSourceDebug` | `{ key: String, tag: String }` | RSS 源调试 |
| `/searchBook` | `{ key: String }` | 搜索在线书籍 |

**注意**: 搜索书籍后需先调用 `/saveBook` 插入书籍才能获取章节和内容。

### Content Provider API

**权限声明**: `io.legado.READ_WRITE`  
**Provider Host**: `包名.readerProvider`（如 `io.legado.app.release.readerProvider`）

#### 书源管理

| URI | 方法 | 说明 |
|-----|------|------|
| `content://providerHost/bookSource/insert` | insert | 插入单个书源 |
| `content://providerHost/bookSources/insert` | insert | 插入多个书源 |
| `content://providerHost/bookSource/query?url=xxx` | query | 获取指定书源 |
| `content://providerHost/bookSources/query` | query | 获取所有书源 |
| `content://providerHost/bookSources/delete` | delete | 删除多个书源 |

**数据格式**: ContentValues 中 `Key="json"`，Value 为 JSON 字符串

#### RSS 源管理

| URI | 方法 | 说明 |
|-----|------|------|
| `content://providerHost/rssSource/insert` | insert | 插入单个 RSS 源 |
| `content://providerHost/rssSources/insert` | insert | 插入多个 RSS 源 |
| `content://providerHost/rssSource/query?url=xxx` | query | 获取指定 RSS 源 |
| `content://providerHost/rssSources/query` | query | 获取所有 RSS 源 |
| `content://providerHost/rssSources/delete` | delete | 删除多个 RSS 源 |

#### 书籍管理

| URI | 方法 | 说明 |
|-----|------|------|
| `content://providerHost/book/insert` | insert | 插入书籍 |
| `content://providerHost/books/query` | query | 获取所有书籍 |
| `content://providerHost/book/chapter/query?url=xxx` | query | 获取章节列表 |
| `content://providerHost/book/content/query?url=xxx&index=1` | query | 获取指定章节内容 |
| `content://providerHost/book/cover/query?path=xxxx` | query | 获取封面 |

**返回数据**: 通过 `Cursor.getString(0)` 获取 JSON 字符串

---

## 数据库 Schema

**数据库名称**: `legado.db`  
**当前版本**: 75  
**ORM 框架**: Room 2.7.1

### 实体列表（21 个）

| 实体类 | 表名 | 说明 |
|--------|------|------|
| `Book` | books | 书籍信息 |
| `BookChapter` | book_chapters | 章节信息 |
| `BookSource` | book_sources | 书源配置 |
| `BookGroup` | book_groups | 书籍分组 |
| `RssSource` | rssSources | RSS 源配置 |
| `RssArticle` | rss_articles | RSS 文章 |
| `RssStar` | rss_stars | RSS 收藏 |
| `RssReadRecord` | rss_read_records | RSS 阅读记录 |
| `Bookmark` | bookmarks | 书签 |
| `ReplaceRule` | replace_rules | 替换规则 |
| `SearchBook` | search_books | 搜索书籍记录 |
| `SearchKeyword` | search_keywords | 搜索关键词 |
| `ReadRecord` | read_records | 阅读记录 |
| `TxtTocRule` | txt_toc_rules | TXT 目录规则 |
| `HttpTTS` | httpTTS | HTTP TTS 配置 |
| `Cache` | cache | 缓存数据 |
| `Cookie` | cookies | Cookie 存储 |
| `RuleSub` | rule_subs | 规则订阅 |
| `DictRule` | dict_rules | 字典规则 |
| `KeyboardAssist` | keyboardAssists | 键盘辅助 |
| `Server` | servers | 服务器配置 |

### 主要表字段说明

#### books 表（书籍信息）

| 字段 | 类型 | 说明 |
|------|------|------|
| `bookUrl` | String | 详情页 URL（主键，本地书源为文件路径） |
| `tocUrl` | String | 目录页 URL |
| `origin` | String | 书源 URL（默认 `BookType.localTag`） |
| `originName` | String | 书源名称或本地文件名 |
| `name` | String | 书籍名称 |
| `author` | String | 作者名称 |
| `kind` | String | 分类信息（书源获取） |
| `customTag` | String | 分类信息（用户修改） |
| `coverUrl` | String | 封面 URL |
| `customCoverUrl` | String | 封面 URL（用户修改） |
| `intro` | String | 简介内容 |
| `customIntro` | String | 简介内容（用户修改） |
| `type` | Int | 类型（文本、音频、图片等） |
| `group` | Long | 自定义分组索引号 |
| `latestChapterTitle` | String | 最新章节标题 |
| `totalChapterNum` | Int | 总章节数 |
| `durChapterIndex` | Int | 当前阅读章节索引 |
| `durChapterPos` | Int | 当前阅读章节位置 |
| `lastCheckTime` | Long | 最后检查更新时间 |
| `lastUpdateTime` | Long | 最后更新时间 |
| `canUpdate` | Boolean | 是否允许更新 |

**索引**: `name` + `author` 唯一索引

#### book_sources 表（书源配置）

| 字段 | 类型 | 说明 |
|------|------|------|
| `bookSourceUrl` | String | 书源地址（主键） |
| `bookSourceName` | String | 书源名称 |
| `bookSourceGroup` | String | 书源分组 |
| `bookSourceType` | Int | 类型（0=文本，1=音频，2=图片，3=文件） |
| `bookUrlPattern` | String | 详情页 URL 正则 |
| `customOrder` | Int | 手动排序编号 |
| `enabled` | Boolean | 是否启用 |
| `enabledExplore` | Boolean | 是否启用发现 |
| `jsLib` | String | JS 库 |
| `enabledCookieJar` | Boolean | 启用 Cookie 自动保存 |
| `concurrentRate` | String | 并发率 |
| `header` | String | 请求头 |
| `loginUrl` | String | 登录地址 |
| `loginUi` | String | 登录 UI |
| `loginCheckJs` | String | 登录检测 JS |
| `coverDecodeJs` | String | 封面解密 JS |
| `bookSourceComment` | String | 注释 |
| `variableComment` | String | 自定义变量说明 |
| `lastUpdateTime` | Long | 最后更新时间 |
| `respondTime` | Long | 响应时间（用于排序） |
| `weight` | Int | 智能排序权重 |
| `searchRule` | SearchRule | 搜索规则（内嵌对象） |
| `exploreRule` | ExploreRule | 发现规则（内嵌对象） |
| `bookInfoRule` | BookInfoRule | 详情规则（内嵌对象） |
| `tocRule` | TocRule | 目录规则（内嵌对象） |
| `contentRule` | ContentRule | 正文规则（内嵌对象） |

**索引**: `bookSourceUrl` 索引

#### rssSources 表（RSS 源配置）

| 字段 | 类型 | 说明 |
|------|------|------|
| `sourceUrl` | String | 源地址（主键） |
| `sourceName` | String | 源名称 |
| `sourceIcon` | String | 源图标 |
| `sourceGroup` | String | 源分组 |
| `sourceComment` | String | 源注释 |
| `enabled` | Boolean | 是否启用 |
| `variableComment` | String | 自定义变量说明 |
| `jsLib` | String | JS 库 |
| `enabledCookieJar` | Boolean | 启用 Cookie 自动保存 |
| `concurrentRate` | String | 并发率 |
| `header` | String | 请求头 |
| `loginUrl` | String | 登录地址 |
| `loginUi` | String | 登录 UI |
| `loginCheckJs` | String | 登录检测 JS |
| `coverDecodeJs` | String | 封面解密 JS |
| `sortUrl` | String | 分类 URL |
| `singleUrl` | Boolean | 是否单 URL 源 |
| `articleStyle` | Int | 列表样式（0/1/2） |
| `ruleArticles` | String | 列表规则 |
| `ruleNextPage` | String | 下一页规则 |
| `ruleTitle` | String | 标题规则 |

**索引**: `sourceUrl` 索引

### DAO 层

每个实体对应一个 DAO 接口，提供 CRUD 操作：

- `BookDao`: 书籍增删改查
- `BookSourceDao`: 书源管理
- `RssSourceDao`: RSS 源管理
- `BookChapterDao`: 章节管理
- `BookmarkDao`: 书签管理
- `ReplaceRuleDao`: 替换规则管理
- ... 共 21 个 DAO

---

## 开发约定

### 代码风格

- **语言**: Kotlin（主应用），Java（book 模块）
- **命名规范**: 遵循 Kotlin 官方编码规范
  - 类名：大驼峰（PascalCase）
  - 函数名/变量名：小驼峰（camelCase）
  - 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
- **包结构**: 按功能模块划分（`data`, `model`, `ui`, `service`, `help`）
- **资源命名**: 小写下划线分隔（`activity_main`, `btn_save`）

### 提交规范

遵循 **Conventional Commits** 规范（参考 `.iflow/IFLOW_GIT.md`）：

```
<type>[(scope)]: <summary>

[body]

[footer]
```

**类型标记**:
- `feat`: 新增功能
- `fix`: 错误修复
- `docs`: 文档变更
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

**示例**:
```
feat(book): 添加 EPUB 3.0 格式支持

fix(source): 修复书源解析空指针异常
```

### 测试约定

- 单元测试位于 `app/src/test/`
- Android 仪器测试位于 `app/src/androidTest/`
- 测试类命名：`<被测试类名>Test`

---

## 关键文件路径

| 文件 | 路径 | 说明 |
|------|------|------|
| 应用入口 | `app/src/main/java/io/legado/app/App.kt` | Application 类 |
| 数据库定义 | `app/src/main/java/io/legado/app/data/AppDatabase.kt` | Room 数据库配置 |
| 主 Activity | `app/src/main/java/io/legado/app/ui/main/MainActivity.kt` | 主界面 |
| 阅读 Activity | `app/src/main/java/io/legado/app/ui/book/read/ReadBookActivity.kt` | 阅读界面 |
| API 控制器 | `app/src/main/java/io/legado/app/api/controller/` | Web API 实现 |
| Web 服务 | `app/src/main/java/io/legado/app/web/` | HTTP/WebSocket 服务 |
| 规则解析 | `app/src/main/java/io/legado/app/model/analyzeRule/` | 书源规则引擎 |
| 构建配置 | `app/build.gradle` | 应用级 Gradle 配置 |
| 依赖版本 | `gradle/libs.versions.toml` | 依赖版本目录 |

---

## 参考文档

- [API 接口文档](api.md)
- [项目 README](README.md)
- [更新日志](CHANGELOG.md)
- [Git 提交规范](.iflow/IFLOW_GIT.md)