# Add project specific ProGuard rules here.

# Keep data classes used with Room
-keep class com.example.kidsstory.data.database.** { *; }
-keep class com.example.kidsstory.domain.models.** { *; }

# Keep Gson models
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Retrofit
-keepattributes Signature
-keepattributes Annotation
-keep class retrofit2.** { *; }

# Keep ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
