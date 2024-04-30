// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.properties["buildDir"])
}

val compileSdk by extra(34)
val minSdk by extra(14)
val targetSdk by extra(34)
val ndkVersion by extra("22.1.7171670")
