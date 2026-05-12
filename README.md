# AI Chat - Android 应用

一个支持自定义 API 密钥和端点的 AI 对话应用。

## 功能特性

- 🎨 深色科技风格 UI (Material Design 3)
- 🔐 安全存储 API 配置
- 🤖 支持 OpenAI 兼容 API
- 💬 实时 AI 对话
- ⚙️ 可自定义模型名称

## 项目结构

```
app/
├── src/main/
│   ├── java/com/aichat/app/
│   │   ├── data/           # 数据层
│   │   │   ├── local/      # 本地存储 (DataStore)
│   │   │   ├── remote/     # 远程 API (Retrofit)
│   │   │   └── repository/ # 数据仓库
│   │   ├── di/             # 依赖注入 (Hilt)
│   │   ├── domain/model/    # 数据模型
│   │   └── ui/             # UI 层
│   │       ├── screens/    # 页面
│   │       └── theme/      # 主题
│   └── res/                # 资源文件
└── build.gradle.kts
```

## 技术栈

- Kotlin 1.9.22
- Jetpack Compose (BOM 2024.02.00)
- Material Design 3
- Hilt (依赖注入)
- Retrofit2 + OkHttp3 (网络请求)
- DataStore Preferences (本地存储)
- Navigation Compose (页面导航)

## 构建步骤

### 方法一：使用 Android Studio

1. **安装 Android Studio**
   - 下载并安装 [Android Studio Hedgehog](https://developer.android.com/studio) 或更高版本

2. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择 `c:\Users\wxl\Desktop\chat` 文件夹

3. **同步项目**
   - Android Studio 会自动下载 Gradle Wrapper 和依赖
   - 等待同步完成（底部进度条）

4. **构建 Debug APK**
   - 菜单栏: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或者点击右侧 Gradle 面板 → app → build → assembleDebug

5. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮 (▶) 或按 Shift + F10

### 方法二：使用命令行

1. **确保已安装 Java JDK 17+**
   ```bash
   java -version
   # 应显示 java 17 或更高版本
   ```

2. **确保已安装 Android SDK**
   - 设置 ANDROID_HOME 环境变量指向 SDK 目录

3. **使用 Gradle Wrapper**
   ```bash
   cd c:\Users\wxl\Desktop\chat
   .\gradlew.bat assembleDebug
   ```

4. **APK 输出位置**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

## 使用说明

### 首次使用

1. 启动应用后，进入设置页面
2. 填写以下信息：
   - **API Endpoint**: AI 服务地址（如 `https://api.openai.com/v1`）
   - **API Key**: 您的 API 密钥
   - **模型**: 模型名称（可选，默认 `gpt-3.5-turbo`）
3. 点击"测试连接"验证配置
4. 点击"保存并开始"进入对话界面

### 对话界面

- 输入消息后点击发送按钮
- 等待 AI 回复
- 点击右上角设置图标可修改配置
- 点击垃圾桶图标可清空对话

## API 配置示例

### OpenAI API
```
Endpoint: https://api.openai.com/v1
API Key: sk-xxxxxxxxxxxxxxxxxxxxxxxx
Model: gpt-3.5-turbo 或 gpt-4
```

### 自定义 API（如本地部署的模型）
```
Endpoint: http://your-server:8000/v1
API Key: your-api-key
Model: your-model-name
```

## 常见问题

### Q: 连接失败？
A: 检查：
1. API Endpoint 格式是否正确（以 /v1 结尾）
2. API Key 是否有效
3. 网络连接是否正常

### Q: 如何更新 API 配置？
A: 在聊天界面点击右上角设置图标

### Q: 对话记录会保存吗？
A: 当前版本仅保留当前会话记录，关闭应用后会清空

## 开发说明

如需修改或扩展功能，请参考 `SPEC.md` 规范文档。

### 添加新的 API 提供商
1. 在 `data/remote/` 创建新的 API 模型
2. 在 `ChatApiService` 添加新的接口
3. 在 `ChatRepository` 实现调用逻辑

## 许可证

本项目仅供学习和参考使用。
