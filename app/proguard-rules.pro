# 保留 Gson 序列化相关类
-keep class com.flashnote.app.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# 保留 Kotlin 协程
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
