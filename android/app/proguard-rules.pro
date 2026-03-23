# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.redfox.game.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase

# OkHttp WebSocket
-keep class okhttp3.internal.ws.** { *; }
-dontwarn okhttp3.internal.platform.**
