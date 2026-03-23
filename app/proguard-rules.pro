-dontwarn androidx.compose.material.**
-dontwarn androidx.compose.ui.res.**
-dontwarn org.jetbrains.jewel.foundation.lazy.SelectableLazyItemScopeDelegate
-dontwarn org.jetbrains.jewel.ui.component.IconKt
-dontwarn com.sun.jna.**
-dontwarn org.jetbrains.jewel.window.utils.macos.MacUtil

# Keep JNA classes
-keep class com.sun.jna.** { *; }
-keepclassmembers class * extends com.sun.jna.Structure { *; }

# Keep Jewel and its JetBrains dependencies
-keep class org.jetbrains.jewel.** { *; }
-keep class com.jetbrains.** { *; }

# Keep Nucleus classes
-keep class io.github.kdroidfilter.nucleus.** { *; }

# Keep fields used by reflection in PlatformUtils.kt
-keepclassmembers class androidx.compose.ui.draganddrop.DragAndDropEvent {
    *;
}
