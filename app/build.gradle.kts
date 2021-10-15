import appdependencies.Builds.APP_ID
import appdependencies.Builds.BUILD_TOOLS
import appdependencies.Builds.COMPILE_VERSION
import appdependencies.Builds.MIN_VERSION
import appdependencies.Builds.TARGET_VERSION
import appdependencies.Libs
import appdependencies.Versions
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = (COMPILE_VERSION)
    buildToolsVersion = BUILD_TOOLS
    defaultConfig {
        applicationId = APP_ID
        minSdk = (MIN_VERSION)
        targetSdk = (TARGET_VERSION)
        //versionCode = appdependencies.Builds.App.VERSION_CODE
        //versionName = appdependencies.Builds.App.VERSION_NAME
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.forEach { variant ->
        variant.outputs.forEach { output ->
            val outputImpl = output as BaseVariantOutputImpl
            val project = project.name
            val sep = "_"
            val flavor = variant.flavorName
            val buildType = variant.buildType.name
            val version = variant.versionName

            val newApkName = "$project$sep$flavor$sep$buildType$sep$version.apk"
            outputImpl.outputFileName = newApkName
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        resources.excludes.add("META-INF/notice.txt")
    }

    // Declare the task that will monitor all configurations.
    configurations.all {
        // 2 Define the resolution strategy in case of conflicts.
        resolutionStrategy {
            // Fail eagerly on version conflict (includes transitive dependencies),
            // e.g., multiple different versions of the same dependency (group and name are equal).
            failOnVersionConflict()

            // Prefer modules that are part of this build (multi-project or composite build) over external modules.
            preferProjectModules()
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(kotlin("stdlib-jdk8", Versions.kotlin))

    implementation(Libs.Core.appcompat)
    implementation(Libs.Core.coreKtx)
    implementation(Libs.Core.constraintlayout)

    implementation(project(":coroutinesmanager"))

    //testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.2")
    testImplementation(Libs.Tests.junit)
    androidTestImplementation(Libs.Tests.runner)
    androidTestImplementation(Libs.Tests.espresso)
}
