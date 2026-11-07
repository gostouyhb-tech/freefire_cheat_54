# Mantém JNI bridges — sem isso a linkagem quebra
-keep class com.bench.ffcheat.NativeLib { *; }
-keepclassmembers class com.bench.ffcheat.NativeLib {
    public static native *;
}

# Mantém serviços acessados pelo sistema
-keep class com.bench.ffcheat.CheatService { *; }
-keep class com.bench.ffcheat.AimAccessibilityService { *; }

# Mantém companion objects do Kotlin
-keepclassmembers class * {
    public static ** Companion;
}
