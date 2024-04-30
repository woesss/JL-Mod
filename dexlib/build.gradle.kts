plugins {
    id("com.android.library")
}

android {
    compileSdk = rootProject.extra["compileSdk"] as Int
    namespace = "ru.playsoftware.j2meloader.dexlib"

    defaultConfig {
        minSdk = rootProject.extra["minSdk"] as Int
        buildConfigField("int", "VERSION_CODE", "1")
    }

    buildFeatures.buildConfig = true

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    lint {
        abortOnError = false
        targetSdk = rootProject.extra["targetSdk"] as Int
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    api("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.ow2.asm:asm:9.6")
}
