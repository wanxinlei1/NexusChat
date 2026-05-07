# AI Chat - Android Application Specification

## 1. Project Overview

**Project Name:** AIChat
**Project Type:** Native Android Application
**Core Functionality:** A chat application that allows users to converse with AI models using their own API key and endpoint configuration.

## 2. Technology Stack & Choices

- **Framework:** Native Android with Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM with Clean Architecture
- **Dependency Injection:** Hilt
- **Networking:** Retrofit + OkHttp + Gson
- **State Management:** StateFlow + ViewModel
- **Coroutines:** Kotlin Coroutines for async operations
- **Local Storage:** DataStore for API key and settings persistence
- **Navigation:** Jetpack Navigation Compose

## 3. Feature List

### Core Features
1. **API Configuration Screen**
   - Input field for API Key (securely stored)
   - Input field for Base URL (customizable endpoint)
   - Input field for Model name
   - Save/Update configuration
   - Clear configuration option

2. **Chat Screen**
   - Message input field with send button
   - Display chat history (user and AI messages)
   - Show loading indicator during API request
   - Display error messages
   - Auto-scroll to latest message
   - Copy message content

3. **Settings**
   - Theme toggle (Light/Dark)
   - Clear chat history
   - App version info

### API Integration
- Support for OpenAI-compatible APIs
- Configurable base URL (supports custom endpoints)
- Streaming response support
- Error handling for network issues and API errors

## 4. UI/UX Design Direction

- **Visual Style:** Material Design 3 with clean, modern aesthetic
- **Color Scheme:** Purple primary color with dynamic theming support
- **Layout:** Single-activity architecture with bottom navigation
  - Two main sections: Chat and Settings
  - Modal dialog for API configuration
- **Typography:** System default fonts with proper hierarchy
- **Animations:** Subtle transitions for messages and loading states
