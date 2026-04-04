# Keep metadata and annotations used by Kotlin/serialization/navigation frameworks.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Keep kotlinx-serialization generated serializer companions.
-if @kotlinx.serialization.Serializable class *
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Compose Multiplatform generated resources accessor classes.
-keep class **.generated.resources.** { *; }

# Keep source and line number info for Crashlytics symbolication.
-keepattributes SourceFile,LineNumberTable
