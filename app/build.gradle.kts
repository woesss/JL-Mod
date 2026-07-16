import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale
import java.util.Properties
import java.util.jar.Attributes
import java.util.jar.Manifest

val versionFile = rootProject.file("version.properties")
require(versionFile.isFile) {
    "Missing version.properties. Restore it from Git before building."
}

val versionProperties = Properties().also { properties ->
    versionFile.inputStream().use(properties::load)
}
val configuredVersionName = versionProperties.getProperty("versionName")?.trim()
    ?: error("version.properties must define versionName.")
val versionMatch = Regex("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)")
    .matchEntire(configuredVersionName)
    ?: error("versionName must use MAJOR.MINOR.PATCH without a prefix or suffix.")
val (versionMajor, versionMinor, versionPatch) = versionMatch.destructured
    .let { (major, minor, patch) -> Triple(major.toLong(), minor.toLong(), patch.toLong()) }
require(versionMinor <= 999 && versionPatch <= 999) {
    "versionName minor and patch components must be between 0 and 999."
}
val configuredVersionCodeLong = versionMajor * 1_000_000 + versionMinor * 1_000 + versionPatch
require(configuredVersionCodeLong in 1..2_100_000_000) {
    "The versionName produces an Android versionCode outside the valid range."
}
val configuredVersionCode = configuredVersionCodeLong.toInt()

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
        resValue(
            "string",
            "crash_report_url",
            secret.getProperty("crashReportUrl", System.getenv("CRASH_REPORT_URL") ?: "")
        )
        resValue(
            "string",
            "crash_report_token",
            secret.getProperty("crashReportToken", System.getenv("CRASH_REPORT_TOKEN") ?: "")
        )
        resValue(
            "string",
            "fingerprint",
            secret.getProperty("fingerprint", System.getenv("CRASH_REPORT_FINGERPRINT") ?: "")
        )
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

    lint {
        // Keep inherited findings visible without allowing new lint problems into CI.
        // Reduce this file gradually as the corresponding code is fixed.
        baseline = file("lint-baseline.xml")

        // AGP's bundled lint cannot resolve the AndroidX/Material inheritance chain
        // for this project and reports every Activity/custom View as non-instantiatable.
        // The affected classes are public and extend the required framework types.
        disable += listOf("MissingTranslation", "Instantiatable")
    }

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
