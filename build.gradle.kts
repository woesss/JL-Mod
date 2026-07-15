// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
}

tasks.register<Delete>("clean") {
    delete(rootProject.properties["buildDir"])
}

val compileSdk by extra(34)
val minSdk by extra(16)
val targetSdk by extra(34)
val ndkVersion by extra("22.1.7171670")
