#!/bin/bash
# Build script for AI Chat Android Application
# This script requires Android SDK to be installed

set -e

echo "AI Chat - Android Build Script"
echo "================================"

# Check if JAVA_HOME is set
if [ -z "$JAVA_HOME" ]; then
    echo "Setting JAVA_HOME..."
    # Try to find Java 17
    if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
    elif [ -d "$HOME/Android/Sdk/jdk" ]; then
        export JAVA_HOME="$HOME/Android/Sdk/jdk"
    else
        echo "Java 17 not found. Please install JDK 17 and set JAVA_HOME"
        exit 1
    fi
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Check if Android SDK is available
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "Setting ANDROID_HOME..."
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    elif [ -d "/usr/local/android-sdk" ]; then
        export ANDROID_HOME="/usr/local/android-sdk"
    else
        echo "Android SDK not found. Please install Android SDK"
        echo "Download from: https://developer.android.com/studio"
        exit 1
    fi
fi

echo "Using ANDROID_HOME: $ANDROID_HOME"

# Build the project
echo ""
echo "Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi
