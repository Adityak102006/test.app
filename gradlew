#!/bin/bash
# Minimal wrapper: tries local gradle or system gradle
if [ -x ./gradlew ]; then
    exec ./gradlew "$@"
fi
if command -v gradle &> /dev/null; then
    exec gradle "$@"
fi
echo "Gradle not found. Install Android SDK / Gradle, then run:"
echo "  ./gradlew :app:assembleDebug"
exit 1
