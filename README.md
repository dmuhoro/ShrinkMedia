# Media Compressor (Android - Kotlin & Jetpack Compose)

A rapid-prototype, high-performance Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Coroutines**, **Coil**, and **FFmpegKit Lite** for compressing images and videos.

## Project Structure
```
├── build.gradle.kts                      # Root Gradle build script
├── settings.gradle.kts                   # Settings and repository configuration
├── gradle.properties                     # JVM and AndroidX properties
└── app/
    ├── build.gradle.kts                  # App module dependencies & SDK configs
    └── src/main/
        ├── AndroidManifest.xml           # App manifest with FileProvider setup
        ├── java/com/example/mediacompressor/
        │   └── MainActivity.kt           # Complete Jetpack Compose UI & Compression Engine
        └── res/
            ├── values/strings.xml        # App resources
            └── xml/file_paths.xml        # FileProvider sharing paths
```

## Features
- **Modern Jetpack Compose UI**: Clean Material 3 design with System, Light, and Dark appearance modes.
- **Single & Batch Mode**: Compress individual files or queue multiple images/videos using `PickMultipleVisualMedia`.
- **Fast Image Compression**: In-memory sampling and scaling with JPEG quality presets (Low, Medium, High).
- **Fast Video Compression**: FFmpegKit Lite CRF & bitrate-constrained encoding (`libx264` + `aac`).
- **Scoped Storage & MediaStore**: Optional auto-save directly to public `Pictures/MediaCompressor` or `Movies/MediaCompressor`.
- **Audit Logs**: Generates `.txt` compression performance reports with space reduction % and processing duration.
- **Android ShareSheet**: One-tap sharing to WhatsApp, Telegram, Gmail, Drive, etc., via `FileProvider`.

## How to Build & Run
1. Clone this repository to your local machine:
   ```bash
   git clone <repo-url>
   ```
2. Open the project root in **Android Studio** (Koala / Ladybug or newer with JDK 17+).
3. Let Gradle sync and download dependencies.
4. Run on any Android device or emulator running **Android 7.0+ (API 24 to 35)**.
