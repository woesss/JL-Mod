import java.util.Locale
import java.util.Properties
import java.util.jar.Attributes
import java.util.jar.Manifest

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = rootProject.extra["compileSdk"] as Int
    ndkVersion = rootProject.extra["ndkVersion"] as String
    namespace = "ru.playsoftware.j2meloader"

    defaultConfig {
        applicationId = "ru.woesss.j2meloader"
        minSdk = rootProject.extra["minSdk"] as Int
        targetSdk = rootProject.extra["targetSdk"] as Int
        versionCode = 47
        versionName = "0.87"
        resValue("string", "app_name", rootProject.name)
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildFeatures {
        viewBinding = true
        prefab = true
        buildConfig = true
    }

    signingConfigs {
        create("emulator") {
            rootProject.file("keystore.properties").takeIf(File::isFile)?.inputStream().use {
                val keystoreProperties = Properties()
                keystoreProperties.load(it)
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
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
        }
    }

    lint {
        disable += "MissingTranslation"
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
            val midletName = props?.getValue("MIDlet-Name")?.trim() ?: "Demo MIDlet"
            val apkName = midletName.replace("[/\\\\:*?\"<>|]".toRegex(), "").replace(" ", "_")
            applicationId = "com.example.androidlet.${apkName.lowercase(Locale.getDefault())}"
            versionName = props?.getValue("MIDlet-Version") ?: "1.0"
            resValue("string", "app_name", midletName)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-midlet.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "armeabi-v7a", "x86_64", "arm64-v8a")
            isUniversalApk = true
        }
    }

    externalNativeBuild {
        ndkBuild {
            path("src/main/cpp/Android.mk")
        }
    }

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
                outputFileName = "${rootProject.name}_$versionName-$dirName.apk"
            }
        }
    }
}

fun getMidletManifestProperties(): Attributes? {
    val mf = Manifest()
    project.file("src/midlet/resources/MIDLET-META-INF/MANIFEST.MF")
        .takeIf(File::isFile)?.inputStream()
        .use(mf::read)
    return mf.mainAttributes
}

dependencies {
    implementation(project(":dexlib"))

    val roomVersion = "2.6.1"
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-rxjava2:$roomVersion")

    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")

    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.arch.core:core-common:2.2.0")
    implementation("androidx.collection:collection-ktx:1.3.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-common:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.transition:transition:1.4.1")
    implementation("com.google.android.material:material:1.11.0")
    //noinspection GradleDependency (next version incompatible with Android 4)
    implementation("com.google.oboe:oboe:1.7.0")

    implementation("ch.acra:acra-dialog:5.11.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.nikita36078:mobile-ffmpeg:v4.3.2-compact")
    implementation("com.github.nikita36078:pngj:2.2.3")
    implementation("com.github.woesss:filepicker:4.4.0")
    implementation("com.github.yukuku:ambilwarna:2.0.1")
    implementation("com.github.penn5:donations:3.6.0")
    //noinspection GradleDependency (next version incompatible with Android 4)
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
