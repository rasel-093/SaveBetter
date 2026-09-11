# SaveBetter ProGuard rules

# Keep Room entity classes
-keep class com.example.savebetter.core.data.local.** { *; }

# Keep Firebase models
-keep class com.example.savebetter.core.data.remote.firebase.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
