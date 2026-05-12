# AI Chat - Android 应用规范

## 1. 项目概述

**项目名称**: AI Chat
**项目类型**: 原生 Android 应用
**核心功能**: 一款支持用户自定义 API 密钥和端点的 AI 对话应用，让用户可以连接自己的 AI 服务进行智能对话

**目标用户**: 
- 开发者和技术爱好者
- 需要使用自己 AI 服务的企业用户
- 希望灵活配置 AI 对话功能的用户

## 2. 技术栈

- **语言**: Kotlin 1.9.x
- **UI 框架**: Jetpack Compose (Material Design 3)
- **最小 SDK**: API 26 (Android 8.0)
- **目标 SDK**: API 34 (Android 14)
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络请求**: Retrofit2 + OkHttp3
- **JSON 解析**: Gson
- **数据存储**: DataStore Preferences
- **协程**: Kotlin Coroutines + Flow
- **Compose 版本**: BOM 2024.02.00

## 3. UI/UX 规范

### 3.1 屏幕结构

应用包含 2 个主要屏幕：
1. **设置屏幕** (Settings Screen) - 首次使用或配置 API
2. **聊天屏幕** (Chat Screen) - 主对话界面

### 3.2 导航流程

```
启动 → 检查配置
    ├─ 未配置 → 设置屏幕 → 聊天屏幕
    └─ 已配置 → 聊天屏幕
```

### 3.3 颜色方案

**主题**: 深色科技风格

**主色调**:
- Primary: #6366F1 (Indigo)
- Primary Container: #4338CA
- Secondary: #22D3EE (Cyan)
- Background: #0F172A (Dark Blue)
- Surface: #1E293B
- On Primary: #FFFFFF
- On Background: #F8FAFC
- On Surface: #E2E8F0

**错误/警告色**:
- Error: #EF4444
- Success: #10B981

### 3.4 排版

- **标题字体**: 系统默认 (Roboto) Bold
- **正文字体**: 系统默认 Regular
- **标题大小**: 24sp
- **正文大小**: 16sp
- **辅助文字**: 14sp

### 3.5 间距规范

- **页面边距**: 16dp
- **组件间距**: 12dp
- **内边距**: 16dp
- **圆角**: 12dp (卡片), 24dp (输入框)

## 4. 功能规范

### 4.1 设置屏幕

**功能**:
- 输入 API Endpoint (URL)
- 输入 API Key (密码样式，可显示/隐藏)
- 输入 Model 名称 (可选，默认: gpt-3.5-turbo)
- 保存配置
- 测试连接按钮

**验证规则**:
- API Endpoint: 必须是有效的 HTTPS URL
- API Key: 不能为空
- Model: 可以为空（使用默认值）

### 4.2 聊天屏幕

**功能**:
- 显示对话历史列表
- 发送消息输入框
- 发送按钮
- 清空对话按钮
- 设置入口按钮

**消息气泡**:
- 用户消息: 右侧对齐，主色调背景
- AI 消息: 左侧对齐，深色背景
- 加载状态: 显示动画点

**输入框**:
- 多行输入
- 最大高度: 120dp
- 发送按钮: 图标按钮

### 4.3 API 集成

**支持 API 格式**: OpenAI 兼容 API

**请求格式**:
```json
POST {endpoint}/chat/completions
Headers:
  Authorization: Bearer {api_key}
  Content-Type: application/json
Body:
{
  "model": "{model}",
  "messages": [
    {"role": "user", "content": "..."}
  ],
  "stream": false
}
```

**响应处理**:
- 解析 choices[0].message.content
- 错误处理: 网络错误、API 错误、Token 限制

## 5. 数据存储

使用 DataStore Preferences 存储:
- `api_endpoint`: String
- `api_key`: String (加密存储)
- `model`: String
- `first_launch`: Boolean

## 6. 项目结构

```
app/
├── src/main/
│   ├── java/com/aichat/app/
│   │   ├── di/                    # 依赖注入
│   │   ├── data/
│   │   │   ├── repository/        # 数据仓库
│   │   │   ├── local/            # 本地存储
│   │   │   └── remote/           # API 服务
│   │   ├── domain/
│   │   │   └── model/            # 数据模型
│   │   ├── ui/
│   │   │   ├── theme/            # 主题
│   │   │   ├── screens/
│   │   │   │   ├── settings/     # 设置页
│   │   │   │   └── chat/         # 聊天页
│   │   │   └── components/       # 通用组件
│   │   └── MainActivity.kt
│   └── res/
└── build.gradle.kts
```

## 7. 验收标准

1. ✅ 用户可以输入并保存 API 配置
2. ✅ 用户可以发送消息并收到 AI 回复
3. ✅ 对话历史正确显示
4. ✅ 应用启动时自动加载已保存的配置
5. ✅ UI 符合深色科技风格设计
6. ✅ 错误情况有友好的提示信息
7. ✅ APK 可以正常安装和运行
