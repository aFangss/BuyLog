# BuyLog

通过解析商品链接来分类记录自己购买过的商品的 App。基于 Kotlin Multiplatform + Compose Multiplatform，一套 UI 代码同时运行在 Android、iOS 和 Web 上。

## 功能

- **首页**：自动检测剪贴板中的商品链接（淘宝/京东口令或 URL），调起解析并弹出商品详情卡片，展示标题、价格、平台、商品图，可选择要保存的图片后存档
- **记录**：商品记录列表（开发中，当前为占位页）
- **设置**：修改昵称；管理各电商平台（淘宝 / 京东 / 拼多多）的 Cookie 等配置
- **商品解析**：接入好单库开放 API（`analyze.clipboard`），各平台按 expect/actual 实现网络层（Android 用 OkHttp，Web 用 fetch）

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / UI | Kotlin 2.0.21、Compose Multiplatform 1.7.3（Material3） |
| 依赖注入 | Koin 4.0.2 |
| 本地存储 | Room（Android）、androidx.sqlite（iOS）、内存实现（Web，刷新即丢失） |
| 偏好设置 | multiplatform-settings |
| 图片加载 | Coil 3 |
| 其他 | kotlinx.coroutines / serialization / datetime、androidx.lifecycle ViewModel |

## 项目结构

```
shared/      共享层：数据模型、网络、数据库抽象、设置、ViewModel（expect/actual 分平台实现）
composeApp/  共享 UI：主题、导航（首页 / 记录 / 设置三个 Tab）、各页面与组件
androidApp/  Android 入口（MainActivity、Koin 初始化）
iosApp/      iOS Xcode 工程（SwiftUI 壳，内嵌 Compose）
webApp/      Web 入口（CanvasBasedWindow + index.html）
scripts/     run-ios-simulator.sh 等辅助脚本
```

## 环境要求

- JDK 17 或更高版本
- Android：Android SDK（compileSdk 36，minSdk 34）
- iOS：macOS + Xcode
- Web：无额外要求，Gradle 会自动下载 Node/Yarn 工具链

## 运行

### Web

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

启动后访问 http://localhost:8080 ，支持热重载。

### Android

```bash
./gradlew :androidApp:installDebug
```

或直接用 Android Studio 打开项目运行 `androidApp`。

### iOS

```bash
./scripts/run-ios-simulator.sh            # 默认 iPhone 16 模拟器
./scripts/run-ios-simulator.sh "iPhone 15"
```

也可在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 运行。

## 注意事项

- 好单库 API 的 `appId` / `appSecret` 目前硬编码在 `shared` 的各平台 `ApiClient` 中，仅供开发使用，发布前请移到安全配置中。
- Web 端的商品记录目前是内存存储，页面刷新后数据会清空；持久化待接入（如 IndexedDB）。
