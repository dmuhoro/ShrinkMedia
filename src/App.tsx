/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState } from 'react';
import { Image, Video, CheckCircle2, AlertCircle, Copy, Check, Smartphone, FileCode2, Sparkles, RefreshCw } from 'lucide-react';

const GRADLE_CODE = `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.mediacompressor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mediacompressor"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core & Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
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

    // Debug Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}`;

const MAIN_ACTIVITY_CODE = `package com.example.mediacompressor

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaCompressorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainCompressorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaCompressorTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFF141218),
            surface = Color(0xFF211F26),
            onPrimary = Color.White,
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFFFEF7FF),
            surface = Color(0xFFF7F2FA),
            onPrimary = Color.White,
            onBackground = Color(0xFF1D1B20),
            onSurface = Color(0xFF1D1B20)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun MainCompressorScreen(modifier: Modifier = Modifier) {
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("No media selected. Choose an image or video to begin.") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Photo Picker Launcher for Images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isLoading = true
            isSuccess = false
            statusMessage = "Image selected: \${uri.lastPathSegment ?: "image"}. Processing compression..."
            
            coroutineScope.launch {
                // Compression logic placeholder execution pipeline
                delay(2000) // Simulating compression background task
                isLoading = false
                isSuccess = true
                statusMessage = "Image compressed successfully! File ready."
            }
        } else {
            statusMessage = "Image selection cancelled."
        }
    }

    // Photo Picker Launcher for Videos
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            isLoading = true
            isSuccess = false
            statusMessage = "Video selected: \${uri.lastPathSegment ?: "video"}. Processing compression..."
            
            coroutineScope.launch {
                // Compression logic placeholder execution pipeline
                delay(3000) // Simulating video compression background task
                isLoading = false
                isSuccess = true
                statusMessage = "Video compressed successfully! Saved to output."
            }
        } else {
            statusMessage = "Video selection cancelled."
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Title and Description
            Text(
                text = "Media Compressor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fast, lightweight media compression powered by Jetpack Compose",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons
            Button(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Select Image",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Select & Compress Image",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Select Video",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Select & Compress Video",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Loading / Progress Indicator Area
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.5.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Compressing...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Card Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSuccess) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )

                    if (selectedUri != null && !isLoading) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "URI: \${selectedUri.toString()}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}`;

export default function App() {
  const [activeTab, setActiveTab] = useState<'preview' | 'gradle' | 'mainActivity'>('preview');
  const [copiedTab, setCopiedTab] = useState<string | null>(null);

  // Live Simulator States
  const [isLoading, setIsLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState('No media selected. Choose an image or video to begin.');
  const [selectedFileName, setSelectedFileName] = useState<string | null>(null);
  const [isSuccess, setIsSuccess] = useState(false);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedTab(id);
    setTimeout(() => setCopiedTab(null), 2000);
  };

  const handleSimulateSelect = (type: 'image' | 'video') => {
    if (isLoading) return;
    const dummyName = type === 'image' ? 'IMG_20260830_1042.jpg' : 'VID_20260830_1045.mp4';
    setSelectedFileName(dummyName);
    setIsLoading(true);
    setIsSuccess(false);
    setStatusMessage(`${type === 'image' ? 'Image' : 'Video'} selected: ${dummyName}. Processing compression...`);

    setTimeout(() => {
      setIsLoading(false);
      setIsSuccess(true);
      setStatusMessage(`${type === 'image' ? 'Image' : 'Video'} compressed successfully! Ready to export.`);
    }, type === 'image' ? 2000 : 3200);
  };

  const handleReset = () => {
    setIsLoading(false);
    setSelectedFileName(null);
    setIsSuccess(false);
    setStatusMessage('No media selected. Choose an image or video to begin.');
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col font-sans">
      {/* Header */}
      <header className="border-b border-neutral-800 bg-neutral-900/80 backdrop-blur-md px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-9 h-9 rounded-xl bg-purple-600/20 border border-purple-500/30 flex items-center justify-center text-purple-400">
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-semibold text-white tracking-tight">Android Media Compressor</h1>
            <p className="text-xs text-neutral-400">Jetpack Compose • Material 3 • Kotlin Coroutines</p>
          </div>
        </div>

        {/* View switcher */}
        <div className="flex items-center gap-1 bg-neutral-800/80 p-1 rounded-xl border border-neutral-700/60">
          <button
            id="tab-preview-btn"
            onClick={() => setActiveTab('preview')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'preview'
                ? 'bg-purple-600 text-white shadow-sm'
                : 'text-neutral-300 hover:text-white'
            }`}
          >
            Live Compose Preview
          </button>
          <button
            id="tab-gradle-btn"
            onClick={() => setActiveTab('gradle')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'gradle'
                ? 'bg-purple-600 text-white shadow-sm'
                : 'text-neutral-300 hover:text-white'
            }`}
          >
            build.gradle.kts
          </button>
          <button
            id="tab-mainactivity-btn"
            onClick={() => setActiveTab('mainActivity')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              activeTab === 'mainActivity'
                ? 'bg-purple-600 text-white shadow-sm'
                : 'text-neutral-300 hover:text-white'
            }`}
          >
            MainActivity.kt
          </button>
        </div>
      </header>

      {/* Content Area */}
      <main className="flex-1 flex overflow-hidden">
        {activeTab === 'preview' && (
          <div className="flex-1 flex flex-col lg:flex-row items-center justify-center p-6 gap-8 overflow-y-auto">
            {/* Phone Frame Simulator */}
            <div className="w-full max-w-sm rounded-[40px] border-4 border-neutral-700 bg-neutral-900 p-3 shadow-2xl relative">
              {/* Notch / Speaker */}
              <div className="absolute top-5 left-1/2 -translate-x-1/2 w-28 h-4 bg-neutral-800 rounded-full z-10 flex items-center justify-center">
                <div className="w-3 h-3 rounded-full bg-neutral-900 border border-neutral-700"></div>
              </div>

              {/* Compose Screen Canvas */}
              <div className="w-full h-[620px] rounded-[30px] bg-[#FEF7FF] text-[#1D1B20] p-6 flex flex-col items-center justify-between pt-12 relative overflow-hidden select-none">
                <div className="w-full text-center mt-4">
                  <h2 className="text-2xl font-bold text-[#1D1B20]">Media Compressor</h2>
                  <p className="text-xs text-neutral-600 mt-2 px-2 leading-relaxed">
                    Fast, lightweight media compression powered by Jetpack Compose
                  </p>
                </div>

                {/* Center Buttons */}
                <div className="w-full space-y-3.5 my-auto">
                  <button
                    id="sim-image-picker-btn"
                    disabled={isLoading}
                    onClick={() => handleSimulateSelect('image')}
                    className="w-full h-14 bg-[#6750A4] hover:bg-[#594294] disabled:opacity-50 text-white rounded-2xl flex items-center justify-center gap-3 font-semibold text-sm shadow-md transition active:scale-[0.98]"
                  >
                    <Image className="w-5 h-5" />
                    Select &amp; Compress Image
                  </button>

                  <button
                    id="sim-video-picker-btn"
                    disabled={isLoading}
                    onClick={() => handleSimulateSelect('video')}
                    className="w-full h-14 bg-[#625B71] hover:bg-[#534d61] disabled:opacity-50 text-white rounded-2xl flex items-center justify-center gap-3 font-semibold text-sm shadow-md transition active:scale-[0.98]"
                  >
                    <Video className="w-5 h-5" />
                    Select &amp; Compress Video
                  </button>
                </div>

                {/* Progress Indicator Area */}
                <div className="h-14 flex flex-col items-center justify-center">
                  {isLoading && (
                    <div className="flex flex-col items-center gap-1.5 animate-in fade-in duration-200">
                      <div className="w-8 h-8 border-3 border-[#6750A4] border-t-transparent rounded-full animate-spin"></div>
                      <span className="text-[11px] font-medium text-[#6750A4]">Compressing...</span>
                    </div>
                  )}
                </div>

                {/* Status Card */}
                <div className="w-full bg-[#F7F2FA] border border-purple-100 rounded-2xl p-4 shadow-sm text-center">
                  <div className="text-[10px] font-bold text-[#6750A4] uppercase tracking-wider mb-1">Status</div>
                  <p className={`text-xs ${isSuccess ? 'text-emerald-700 font-semibold' : 'text-neutral-700'}`}>
                    {statusMessage}
                  </p>
                  {selectedFileName && !isLoading && (
                    <div className="mt-2 text-[10px] text-neutral-400 truncate">
                      URI: content://media/external/{selectedFileName}
                    </div>
                  )}
                </div>

                {/* Bottom Bar indicator */}
                <div className="w-32 h-1 bg-neutral-300 rounded-full mt-3"></div>
              </div>
            </div>

            {/* Side Information & Quick Actions */}
            <div className="w-full max-w-md space-y-4">
              <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
                <div className="flex items-center gap-2 text-purple-400 font-semibold text-sm mb-2">
                  <Sparkles className="w-4 h-4" />
                  <span>Phase 1 &amp; Phase 2 Completed</span>
                </div>
                <p className="text-neutral-300 text-sm leading-relaxed mb-4">
                  The Android Jetpack Compose code structure is ready with modern Material 3 styling, 
                  <code className="text-purple-300 bg-neutral-800 px-1 py-0.5 rounded text-xs ml-1">PickVisualMedia</code> activity contracts, and reactive coroutine loading handlers.
                </p>

                <div className="space-y-2.5 text-xs text-neutral-300">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>Target SDK: 35 (Android 15 ready), Min SDK: 24</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>Edge-to-Edge display with Compose BOM 2024.10.00</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                    <span>No runtime storage permissions required (Modern Photo Picker)</span>
                  </div>
                </div>

                <div className="mt-5 flex gap-3">
                  <button
                    onClick={() => setActiveTab('mainActivity')}
                    className="flex-1 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold py-2.5 px-4 rounded-xl transition flex items-center justify-center gap-2"
                  >
                    <FileCode2 className="w-4 h-4" />
                    View MainActivity.kt
                  </button>
                  <button
                    onClick={handleReset}
                    className="bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs font-medium py-2.5 px-4 rounded-xl transition flex items-center gap-2"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    Reset Sim
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'gradle' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/build.gradle.kts</span>
                <span className="text-[10px] bg-neutral-800 text-purple-400 px-2 py-0.5 rounded-full border border-neutral-700">
                  Kotlin DSL
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(GRADLE_CODE, 'gradle')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'gradle' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'gradle' ? 'Copied' : 'Copy build.gradle.kts'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{GRADLE_CODE}</code>
            </pre>
          </div>
        )}

        {activeTab === 'mainActivity' && (
          <div className="flex-1 p-6 flex flex-col h-full overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <span className="text-xs font-mono text-neutral-400">app/src/main/java/com/example/mediacompressor/MainActivity.kt</span>
                <span className="text-[10px] bg-neutral-800 text-purple-400 px-2 py-0.5 rounded-full border border-neutral-700">
                  Jetpack Compose
                </span>
              </div>
              <button
                onClick={() => copyToClipboard(MAIN_ACTIVITY_CODE, 'mainActivity')}
                className="flex items-center gap-1.5 bg-purple-600 hover:bg-purple-500 text-white text-xs font-medium px-3 py-1.5 rounded-lg transition"
              >
                {copiedTab === 'mainActivity' ? <Check className="w-3.5 h-3.5" /> : <Copy className="w-3.5 h-3.5" />}
                {copiedTab === 'mainActivity' ? 'Copied' : 'Copy MainActivity.kt'}
              </button>
            </div>
            <pre className="flex-1 bg-neutral-900 border border-neutral-800 rounded-xl p-4 text-xs font-mono text-neutral-300 overflow-auto leading-relaxed">
              <code>{MAIN_ACTIVITY_CODE}</code>
            </pre>
          </div>
        )}
      </main>
    </div>
  );
}

