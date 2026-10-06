# EN.MATH

Android starter project built with Kotlin, Jetpack Compose, and Material 3.

## Open and run

1. Open this directory in Android Studio.
2. Use JDK 17 for Gradle and install Android SDK 35.
3. Let Gradle sync, select an emulator or device running Android 7.0+ (API 24), and run `app`.

Command-line build: `./gradlew assembleDebug` on macOS/Linux, or `gradlew.bat assembleDebug` on Windows.

## Structure

- `app/src/main/java/com/ahmadmol/enmath/MainActivity.kt`: Compose entry point and welcome screen.
- `app/src/main/res/values/`: app strings and launch theme.
- `app/build.gradle.kts`: Android and Compose dependencies.

The initial screen is a starter placeholder. Application features can be added later.

## Toolchain

AGP 8.9.2, Gradle 8.11.1, Kotlin / Compose compiler plugin 2.1.20,
Compose BOM 2025.04.01, compile/target SDK 35, minimum SDK 24.

No application build has been verified in the preparation environment because JDK and Android SDK are unavailable there.
