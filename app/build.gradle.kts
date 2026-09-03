import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // Production signing. Reads an optional local keystore.properties (gitignored).
    // Without it, the release buildType falls back to unsigned and the release
    // AAB cannot be produced — a keystore must be present on the packaging host.
    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties().apply {
        if (keystorePropsFile.exists()) load(keystorePropsFile.inputStream())
    }
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }
    namespace = "com.shrinkmedia.compressor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shrinkmedia.compressor"
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "0.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core & Lifecycle ViewModel Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI & Material 3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Coil Image & Video Loading for Jetpack Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-video:2.7.0")

    // Jetpack DataStore Preferences for Persistent Settings
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ML Kit on-device text recognition (OCR) — runs locally, no INTERNET.
    // New dependency justified: no existing lib (FFmpegKit/Coil/DataStore/
    // android.graphics.pdf) provides OCR; ML Kit is on-device and privacy-safe.
    // PRIVACY (AGENTS §2): ML Kit's telemetry backend keeps its manifest's
    // android.permission.INTERNET out of the shipped APK via a
    // tools:node="remove" rule in AndroidManifest.xml (the CCT backend cannot be
    // dependency-excluded because ML Kit's internal initializer
    // com.google.android.gms.internal.mlkit_common.zzsp hard-references
    // CCTDestination and R8 fails to minify without it).
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // ML Kit GenAI Prompt (Gemini Nano via AICore) — ADR-011. On-device, device-gated.
    // NO INTERNET surfaces past the merged-manifest tools:node="remove" guard (CI enforces it).
    // Transitive genai-common is included. Compiled against Kotlin 2.2 (see L4a bump).
    implementation("com.google.mlkit:genai-prompt:1.0.0-beta2")

    // FFmpegKit FULL (video compression engine; LGPL, maintained Central-published
    // fork exposing the com.arthenica.ffmpegkit API used by MainActivity).
    // JUSTIFICATION (AGENTS §6): the previous audio-only "LTS/Lite" build
    // (io.github.nikita36078:ffmpeg-kit:6.0.LTS) was proven on-device to contain NO
    // H.264 encoder, NO x264, and NO MP4 muxer (config showed only wav/pcm/aresample
    // after --disable-everything) — so compressVideoFile always failed. The `full`
    // variant (audio + video + https) ships the openh264 H.264 encoder + MP4 muxer;
    // non-GPL so it does not copyleft the closed-source app.
    implementation("dev.ffmpegkit-maintained:ffmpeg-kit-full:8.1.7")

    // FFmpegKit runtime companion: FFmpegKitConfig.<clinit> resolves
    // com.arthenica.smartexception.java.Exceptions at JVM-init time. The full
    // 8.1.7 AAR does not declare it transitively (the old nikita36078 POM did), so
    // it must be pulled explicitly or the app crashes NoClassDefFoundError on first
    // FFmpegKit use.
    implementation("com.arthenica:smart-exception-java:0.2.1")

    // iText7 (on-device PDF engine). New dependency justified (AGENTS §6):
    // android.graphics.pdf can render/merge pages but CANNOT extract embedded text
    // and rasterizes on merge, and no existing lib (FFmpegKit/Coil/DataStore/ML Kit)
    // provides it. iText7 PdfTextExtractor is the only way to read real, compressed
    // PDF text streams on-device (fixes the special-character bug); it also builds
    // true vector PDFs. Runs fully locally — no INTERNET permission.
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Debug Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Instrumented test tooling
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Local JVM unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
