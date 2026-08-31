# FFmpegKit (com.arthenica.*) uses reflection and JNI to dispatch commands.
# R8 must not strip or rename these classes/members at release time.
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.ffmpegkit.ffmpeg.** { *; }
-keep class com.arthenica.ffmpegkit.avfilter.** { *; }

# Keep the public surface of the compressor package.
-keep class com.shrinkmedia.compressor.CompressionQuality { *; }
