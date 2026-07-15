import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale
import java.util.Properties
import java.util.jar.Attributes
import java.util.jar.Manifest

val configuredVersionName = providers.environmentVariable("VERSION_NAME").orNull
    ?.removePrefix("v")
    ?: "0.87.1"
val configuredVersionCode = providers.environmentVariable("VERSION_CODE").orNull
    ?.toIntOrNull()
    ?: 48

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val secret = Properties().also { properties ->
    rootProject.file("keystore.properties").runCatching { inputStream().use(properties::load) }
}

android {
    compileSdk = rootProject.extra["compileSdk"] as Int
    ndkVersion = rootProject.extra["ndkVersion"] as String
    namespace = "ru.playsoftware.j2meloader"

    defaultConfig {
        applicationId = "io.github.h3nb.jlmodplus"
        minSdk = rootProject.extra["minSdk"] as Int
        targetSdk = rootProject.extra["targetSdk"] as Int
        versionCode = configuredVersionCode
        versionName = configuredVersionName
        resValue("string", "app_name", "JL-Mod Plus")
        resValue("string", "app_center", secret.getProperty("appCenterKey", ""))
        resValue("string", "fingerprint", secret.getProperty("fingerprint", ""))
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    @Suppress("UnstableApiUsage")
    androidResources.generateLocaleConfig = true

    buildFeatures {
        viewBinding = true
        prefab = true
        buildConfig = true
    }

    signingConfigs.create("emulator") {
        if (secret.isNotEmpty()) {
            keyAlias = secret.getProperty("keyAlias")
            keyPassword = secret.getProperty("keyPassword")
            storeFile = rootProject.file(secret.getProperty("storeFile"))
            storePassword = secret.getProperty("storePassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
        debug {
            applicationIdSuffix = ".debug"
            isJniDebuggable = true
            multiDexEnabled = true
            multiDexKeepProguard = file("multidex-config.pro")
            ndk {
                abiFilters += "arm64-v8a"
            }
        }
    }

    lint.disable += "MissingTranslation"

    flavorDimensions += "default"
    productFlavors {
        create("emulator") { // variant dimension for create emulator
            buildConfigField("boolean", "FULL_EMULATOR", "true")
            signingConfig = signingConfigs.getByName("emulator")
            versionNameSuffix = System.getenv("VERSION_SUFFIX")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("midlet") { // variant dimension for create android port from J2ME app source
            buildConfigField("boolean", "FULL_EMULATOR", "false")
            // configure midlet's port project params here, as default it read from app manifest,
            // placed to 'app/src/midlet/resources/MIDLET-META-INF/MANIFEST.MF'
            val props = getMidletManifestProperties()
            val midletName = props.getValue("MIDlet-Name")?.trim() ?: "Demo MIDlet"
            val apkName = midletName.replace("[/\\\\:*?\"<>|]".toRegex(), "").replace(" ", "_")
            applicationId = "com.example.androidlet.${apkName.lowercase(Locale.getDefault())}"
            versionName = props.getValue("MIDlet-Version") ?: "1.0"
            resValue("string", "app_name", midletName)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-midlet.pro"
            )
        }
    }

    externalNativeBuild.ndkBuild.path("src/main/cpp/Android.mk")

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    applicationVariants.configureEach {
        if (buildType.name == "debug" && flavorName == "emulator") {
            resValue("string", "app_name", "JL-Debug")
        }
        outputs.configureEach {
            if (this is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                outputFileName = "${rootProject.name}_$versionName-$flavorName-${buildType.name}.apk"
            }
        }
    }
}

kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_17)

fun getMidletManifestProperties(): Attributes = Manifest().let { mf ->
    project.file("src/midlet/resources/MIDLET-META-INF/MANIFEST.MF").runCatching {
        inputStream().use(mf::read)
    }
    return mf.mainAttributes
}

dependencies {
    implementation(projects.dexlib)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.arch.core.common)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.rxjava2)
    implementation(libs.androidx.transition)

    annotationProcessor(libs.google.auto.service)
    compileOnly(libs.google.auto.service.annotations)
    implementation(libs.google.gson)
    implementation(libs.google.material)
    implementation(libs.google.oboe)

    implementation(libs.acra.http)
    implementation(libs.ambilwarna)
    implementation(libs.donations)
    implementation(libs.ffmpeg.mobile)
    implementation(libs.filepicker)
    implementation(libs.pngj)
    implementation(libs.rx.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
