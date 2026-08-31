#!/bin/bash
# Build script for LunarMatcher APK
# Requires Android SDK / Gradle installed

set -e
if [ -x ./gradlew ]; then
    ./gradlew :app:assembleDebug
elif command -v gradle &> /dev/null; then
    gradle :app:assembleDebug
else
    echo "Gradle not found. Install Android Studio or Gradle, then run:"
    echo "  ./gradlew :app:assembleDebug"
    exit 1
fi

echo "APK output: app/build/outputs/apk/debug/app-debug.apk"
echo "Copy this APK to repo for testing."
