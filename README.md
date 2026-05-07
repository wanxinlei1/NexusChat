# AI Chat - Android Application

A native Android chat application that enables users to communicate with AI models using their own API keys and customizable endpoints.

## Features

- **Custom API Configuration**: Users can input their own API key, base URL, and model name
- **Chat Interface**: Modern Material Design 3 chat UI with user and AI message bubbles
- **Message History**: In-memory chat history during session
- **Copy Messages**: Easy copy functionality for AI responses
- **Theme Support**: Light and dark theme with dynamic colors
- **Error Handling**: Comprehensive error messages for API and network issues

## Architecture

The application follows **Clean Architecture** with **MVVM** pattern:

```
com.aichat.app/
├── data/                    # Data layer
│   ├── api/                 # Retrofit API services
│   ├── local/               # DataStore and local storage
│   ├── model/               # Data transfer objects
│   └── repository/          # Repository implementations
├── domain/                  # Domain layer
│   ├── model/               # Domain models
│   └── repository/          # Repository interfaces
├── di/                     # Dependency injection modules
└── ui/                     # Presentation layer
    ├── components/          # Reusable UI components
    ├── navigation/          # Navigation configuration
    ├── screens/             # Screen composables and ViewModels
    └── theme/               # Material 3 theming
```

## Technology Stack

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - UI components and theming
- **Hilt** - Dependency injection
- **Retrofit** - Network HTTP client
- **OkHttp** - HTTP client with logging
- **Gson** - JSON serialization
- **DataStore** - Preferences storage
- **Navigation Compose** - In-app navigation
- **Coroutines** - Asynchronous programming

## Prerequisites

Before building, ensure you have:

1. **Java Development Kit (JDK)**
   - JDK 17 or higher recommended
   - Set `JAVA_HOME` environment variable

2. **Android SDK**
   - Install Android SDK 34 (API Level 34)
   - Set `ANDROID_HOME` environment variable
   - Install build tools

3. **Gradle**
   - The project includes Gradle wrapper
   - System Gradle 8.x can also be used

## Building the Application

### Quick Build

Run the build script:
```bash
chmod +x build.sh
./build.sh
```

### Manual Build

1. Set environment variables:
```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
```

2. Build the debug APK:
```bash
./gradlew assembleDebug
```

3. The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Variants

- **Debug**: `./gradlew assembleDebug`
- **Release**: `./gradlew assembleRelease`

## Installation

1. Transfer the APK to your Android device
2. Enable "Install from unknown sources" in device settings
3. Install the APK
4. Launch AI Chat and configure your API settings

## Usage

### First Time Setup

1. Launch the app
2. Tap the settings icon (⚙️) in the top bar
3. Enter your API configuration:
   - **API Key**: Your OpenAI or compatible API key
   - **Base URL**: API endpoint (default: https://api.openai.com/v1/)
   - **Model**: AI model to use (default: gpt-3.5-turbo)
4. Tap "Save"

### Chatting

1. Type your message in the input field
2. Tap the send button or press Enter
3. Wait for the AI response
4. Tap the copy icon on AI messages to copy content

### Settings

- Access settings via the bottom navigation bar
- Clear chat history using the refresh icon in the chat screen

## Configuration Examples

### OpenAI
- Base URL: `https://api.openai.com/v1/`
- Model: `gpt-3.5-turbo` or `gpt-4`

### Azure OpenAI
- Base URL: `https://your-resource.openai.azure.com/` (without /v1/)
- Model: `gpt-35-turbo` or `gpt-4`

### Custom/Other APIs
- Base URL: Your custom API endpoint
- Model: Your specified model name

## Permissions

The app requires:
- **INTERNET**: For API communication

## Security Notes

- API keys are stored locally using encrypted DataStore
- Never share your API key or commit it to version control
- The app does not transmit data except to your configured API endpoint

## Troubleshooting

### Build Errors

1. **Plugin not found**: Ensure Android SDK is installed and `ANDROID_HOME` is set
2. **Java version error**: Use JDK 17+ and ensure `JAVA_HOME` points to it
3. **SDK not found**: Install Android SDK 34 and set `ANDROID_HOME`

### Runtime Errors

1. **API errors**: Check your API key and endpoint configuration
2. **Network errors**: Ensure internet connectivity and correct proxy settings
3. **Empty responses**: Verify model name is valid for your API

## Development

### Project Structure

The main code files are located in:
- **UI Layer**: `app/src/main/java/com/aichat/app/ui/`
- **Data Layer**: `app/src/main/java/com/aichat/app/data/`
- **Domain Layer**: `app/src/main/java/com/aichat/app/domain/`

### Adding Dependencies

Edit `app/build.gradle.kts` to add new dependencies.

### Running Tests

```bash
./gradlew test
```

## License

This project is for educational and personal use. Respect API provider terms of service.

## Contributing

Contributions are welcome! Please ensure code follows existing patterns and passes linting.
