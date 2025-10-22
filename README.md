<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121936190.png" width="200" alt="应用图标">
</div>

<p align="center">
  <img alt="license" src="https://img.shields.io/github/license/Lonely0710/travel-in-android?style=flat-round" />
  <img alt="gitHubtoplanguage" src="https://img.shields.io/github/languages/top/Lonely0710/travel-in-android" />
  <img alt="issues" src="https://img.shields.io/github/issues/Lonely0710/travel-in-android" />
  <img alt="contributors" src="https://img.shields.io/github/contributors/Lonely0710/travel-in-android" />
  <img alt="stars" src="https://img.shields.io/github/stars/Lonely0710/travel-in-android" />
  <!-- <img alt="release" src="https://img.shields.io/github/v/release/Lonely0710/drama-tracker-Android" /> -->
  <!-- <img alt="downloads" src="https://img.shields.io/github/downloads/Lonely0710/drama-tracker-Android/total" /> -->
</p>

# Traveler - 你的专属旅行助手

> 一款精致的旅行规划与分享平台，记录你的每一次精彩探索

## 🌟 功能全景

### 📱 核心功能
| 模块     | 特性                       | 技术亮点                        |
| -------- | -------------------------- | ------------------------------- |
| **发现** | 智能推荐/内容浏览/互动社区 | AI推荐算法 + Compose UI         |
| **路线** | 智能规划/行程管理/足迹记录 | 路线算法 + Room持久化           |
| **地图** | 地理标记/POI检索/导航指引  | Mapbox/高德地图API + 自定义图层 |
| **同步** | 多端实时同步/离线访问      | Bmob 后端云             |

## 🖼 界面展示

### 🚀 启动与欢迎
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121906169.png" width="30%" alt="启动欢迎页1">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121906208.png" width="30%" alt="启动欢迎页2">
  
  应用启动与欢迎流程
</div>

---

### 🔐 身份验证
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121907642.png" width="30%" alt="登录界面"> 
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121907652.png" width="30%" alt="注册界面">
  
  用户登录与注册流程
</div>

---

### 🏠 主页交互
<div align="center">
  <div>
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121907782.png" width="30%" alt="主页首页">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911271.png" width="30%" alt="国内旅行">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911144.png" width="30%" alt="出境旅行">
  </div>
  
  <div style="margin-top:20px">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911828.png" width="30%" alt="周边游">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911891.png" width="30%" alt="主题游">
    <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911149.png" width="30%" alt="个人中心">
  </div>
  
  主页功能与分类浏览
</div>

---

### 🌍 探索与分享
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934270.png" width="30%" alt="探索推荐">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934456.png" width="30%" alt="发现更多">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934376.png" width="30%" alt="帖子详情">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934327.png" width="30%" alt="VR体验">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934251.png" width="30%" alt="发布帖子">
  
  探索发现与内容分享
</div>

---

### 🗺️ 路线规划
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934302.png" width="30%" alt="路线主页">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934312.png" width="30%" alt="计划行程">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121934319.png" width="30%" alt="我的路线">
  
  智能路线规划与管理
</div>

---

### 👤 个人中心
<div align="center">
  <img src="https://lonelynotes-images.oss-cn-beijing.aliyuncs.com/202506121911149.png" width="30%" alt="个人资料管理">
  
  账户与个人信息管理
</div>

## 🏗️ 技术架构

### 系统设计
```text
架构模式: 单Activity多Fragment
开发语言: Java
核心组件:
  ├── 数据层: Repository模式 + Bmob后端云SDK + SQLite本地持久化
  ├── 展示层: ViewBinding + LiveData + Material Design
  ├── 业务层: 模块化Fragment设计
  └── 工具层: OkHttp网络请求 + Glide图片处理 + Lottie动画 + 百度地图SDK
```

### 核心技术栈
- **UI框架**: Android原生视图 (XML + ViewBinding), Material Design 3
- **网络请求**: OkHttp (高效HTTP客户端), 用于与第三方API（如天气、维基百科、Unsplash等）交互。
- **数据存储**: Bmob后端云 (云端数据管理与同步), SQLite (本地数据持久化，用于本地缓存或离线数据)
- **图片加载**: Glide (高性能图片加载与缓存)
- **动画**: Lottie (解析并播放After Effects动画)
- **响应式编程**: RxJava3 & RxAndroid (处理异步数据流)
- **JSON解析**: Gson (Java对象与JSON数据转换)
- **地图服务**: 百度地图SDK (提供地图展示、POI检索、导航等功能)
- **文本处理**: Jsoup (HTML解析，用于抓取网页内容), Pinyin4j (汉字转拼音)
- **位置服务**: Google Play Services Location (获取用户位置信息)
- **UI组件**: CircleImageView (圆形图片显示)

## ⚙️ 配置指南

### 环境要求
- Android Studio Flamingo+
- Java 11+
- Bmob后端云服务实例
- 百度地图开发者账号

### 密钥文件配置
为了保护您的API密钥，请在 `app/src/main/assets/` 目录下创建 `secrets.properties` 文件，并填写以下内容：
```properties
BMOB_APP_ID=您的Bmob应用ID
BAIDU_LBS_API_KEY=您的百度地图API Key
DEEPSEEK_API_KEY=您的DeepSeek API Key
UNSPLASH_ACCESS_KEY=您的Unsplash Access Key
QWEATHER_API_KEY=您的和风天气API Key
```
这些密钥将用于应用中的相应服务。

### 云服务配置
1.  **注册Bmob开发者账号**：访问 [Bmob官网](https://www.bmob.cn/) 注册并创建您的应用。
2.  **配置Bmob SDK**：
    在 `app/build.gradle.kts` 中确认Bmob SDK依赖已添加：
    ```kotlin
    // Bmob 后端云
    implementation ("io.github.bmob:android-sdk:4.1.0")
    implementation ("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    ```
3.  **初始化Bmob**：
    在您的应用启动类（通常是 `TravelerApplication.java` 或 `MainActivity.java`）中，使用您的 Bmob Application ID 和 REST API Key 初始化SDK。
    ```java
    // 示例代码，请根据Bmob官方文档进行实际配置
    // Bmob.initialize(this, "您的Application ID", "您的REST API Key");
    ```
    请根据您的项目实际情况，在代码中找到Bmob初始化的地方，并替换为您的密钥。
4.  **百度地图SDK配置**：
    前往 [百度地图开放平台](http://lbsyun.baidu.com/) 注册开发者账号并创建应用，获取您的AK (Access Key)。
    在 `AndroidManifest.xml` 中配置百度地图相关的Key和服务，例如：
    ```xml
    <!-- ... 其他配置 ... -->
    <meta-data
        android:name="com.baidu.lbsapi.API_KEY"
        android:value="您的百度地图AK" />
    <!-- ... 其他配置 ... -->
    ```
    确保 `app/libs/BaiduLBS_Android.aar` 文件已正确放置在项目中。

## 📦 安装与使用

### APK安装
```bash
adb install app/release/travelin_release_v1.0.apk
```

## 🌱 贡献指引
欢迎通过以下方式参与项目：
- 在Issues报告问题或建议
- 提交Pull Request时请：
  - 遵循现有代码风格
  - 更新相关文档
  - 添加必要的单元测试

## 📜 许可协议
本项目基于 [MIT License](LICENSE) 开源，允许自由使用和修改，但需保留原始版权声明。

---

<details>
<summary>📮 联系维护者</summary>

**核心开发者**：[Lonely0710](https://github.com/Lonely0710), [mikasa-s](https://github.com/mikasa-s), [diyiycw](https://github.com/diyiycw)
**技术栈咨询**：欢迎提交Issue讨论
**路线图**：
- [x] 基础功能实现 (2025 Q1)
- [ ] 智能推荐系统优化 (2025 Q3)
- [ ] 路线规划AI辅助 (2026 Q1)
</details>
