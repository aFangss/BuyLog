# AGENTS.md — BuyLog 编程规范

本文档面向参与本项目的开发者与 AI 编程助手，描述代码约定与协作规则。**本文件与 `README.md` 必须随代码变化同步更新**（见文末"文档同步"）。

## 项目概览

BuyLog 是 Kotlin Multiplatform + Compose Multiplatform 项目，一份代码跑 Android / iOS / Web 三端。商品解析接入好单库 API。模块职责：

- `shared/`：数据与业务层 —— `data/model`、`data/network`、`data/db`、`data/local`、`platform`、`viewmodel`、`di`
- `composeApp/`：共享 UI 层 —— `ui/screens`、`ui/components`、`ui/navigation`、`ui/theme`
- `androidApp/` / `iosApp/` / `webApp/`：各端入口，只做启动与平台端 Koin 模块装配，不写业务逻辑

## 语言与文案

- 代码（类名、函数、变量）用英文，命名遵循 Kotlin 官方风格
- UI 文案、日志、错误提示用中文（如 `"解析失败，请检查网络或链接是否正确"`）
- 注释与提交信息跟随仓库现有习惯，当前以中文为主

## 架构与分层约定

- **commonMain 只放纯共享逻辑**。任何平台相关能力（网络、数据库、剪贴板、设置存储等）一律用 `expect`/`actual` 抽象，actual 实现放对应 sourceSet（`androidMain` / `iosMain` / `jsMain`），参考 `HaodankuApiClient`、`createAppDatabase`、`createSettings`
- **领域模型与存储实体分离**：跨平台代码只使用 `commonMain` 的 `Product` 等模型；Room 实体（`RoomProduct`）隔离在 `androidMain`，通过 `ProductDao` 接口适配，不外泄到 UI 层
- **状态管理**：一律 `ViewModel` + `StateFlow`（`MutableStateFlow` + `asStateFlow()` 暴露只读流），UI 层不直接持有可变状态
- **依赖注入**：统一用 Koin。共享依赖注册在 `shared/di/Modules.kt` 的 `sharedModule`；平台特有依赖（如 `AppDatabase`、`Settings`）注册在各入口模块（`AndroidModule` / `WebModule` / `IosModule`）
- **UI**：Material3；颜色/字体/主题集中在 `composeApp/ui/theme/`，页面中不写死颜色值；底部导航统一用 `CapsuleBottomNav`

## 依赖管理

- 所有依赖与版本统一登记在 `gradle/libs.versions.toml`，新增依赖先加 catalog 再在各模块 `build.gradle.kts` 引用（`libs.xxx`）
- 不要把版本号硬编码进模块 build 文件（现有少量历史遗留除外，改动时顺手收编进 catalog）
- 优先使用项目已引入的库解决问题，新增第三方库前先确认没有现成方案

## 网络与密钥

- 新增商品解析/平台 API 时，在各平台的 `ApiClient` actual 实现中按同一签名补齐
- 好单库 `appId`/`appSecret` 目前硬编码在各平台 `ApiClient` 中，属已知技术债；不要扩散到更多文件，后续应统一收敛到安全配置

## 变更验证

改动代码后，至少验证受影响平台的编译/运行：

- Web：`./gradlew :webApp:jsBrowserDevelopmentRun`（访问 http://localhost:8080 ）
- Android：`./gradlew :androidApp:assembleDebug`
- iOS：`./scripts/run-ios-simulator.sh`（需 macOS）

修改了 `shared/` 的 commonMain 意味着三端都受影响，需全部验证。

## 文档同步（强制）

`README.md` 与 `AGENTS.md` 是项目的一部分，**任何代码变更只要触及以下内容，必须在同一次改动中同步更新文档**：

- 功能增删或页面变化（如"记录"页从占位变为实现）→ 更新 README"功能"一节
- 依赖、插件、版本变化（`libs.versions.toml`）→ 更新 README"技术栈"与本文件相关约定
- 模块结构、目录职责变化 → 更新两处文档的"项目结构"
- 运行命令、环境要求变化 → 更新 README"运行"/"环境要求"
- 编程约定本身调整 → 更新本文件

写文档时如实反映现状：未完成的功能标注"开发中/占位"，已知问题写进"注意事项"，不要把计划中的能力描述成已有的。
