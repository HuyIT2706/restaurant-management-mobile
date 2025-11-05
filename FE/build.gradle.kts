// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Ensure Gradle plugin classpath (including Hilt plugin workers) uses a compatible JavaPoet
buildscript {
    configurations.classpath {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.squareup" && requested.name == "javapoet") {
                useVersion("1.13.0")
                because("Hilt Gradle plugin requires javapoet with canonicalName() method")
            }
        }
    }
}