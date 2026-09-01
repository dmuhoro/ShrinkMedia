# FFmpegKit (com.arthenica.*) uses reflection and JNI to dispatch commands.
# R8 must not strip or rename these classes/members at release time.
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.ffmpegkit.ffmpeg.** { *; }
-keep class com.arthenica.ffmpegkit.avfilter.** { *; }

# Keep the public surface of the compressor package.
-keep class com.shrinkmedia.compressor.CompressionQuality { *; }

# iText7 (on-device PDF engine) — keep core classes R8 would otherwise strip.
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# iText7 depends on the slf4j-api logging facade. The binding implementation
# (org.slf4j.impl.StaticLoggerBinder) is optional at runtime — iText logs via
# slf4j if a provider is present, otherwise falls back to a harmless
# "No SLF4J providers" notice. R8 flags the missing optional binding; suppress it.
-dontwarn org.slf4j.impl.StaticLoggerBinder
