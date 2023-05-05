//------ APP VERSION
extra["appVersion"] = "1.2.8"
extra["versionName"] = "1.4.5"

//------ CONFIG DATA
extra["appId"] = "com.mincor.kodiexample"
extra["minSdkVersion"] = 18
extra["buildSdkVersion"] = 33
extra["kotlinApiVersion"] = "1.8"
extra["jvmVersion"] = "11"
extra["agpVersion"] = "7.4.2"
extra["kotlinVersion"] = "1.8.21"
extra["jitpackPath"] = "https://jitpack.io"
extra["pluginsPath"] = "https://plugins.gradle.org/m2/"
extra["codePath"] = "src/main/kotlin"
extra["resPath"] = "src/main/res"

//------- LIBS VERSIONS
val coroutines = "1.7.0-RC"
val core: String = "1.10.0"
val constraintLayout = "2.1.4"
val material = "1.8.0"
val appcompat = "1.6.1"

//------- Libs path
extra["appcompat"] = "androidx.appcompat:appcompat:$appcompat"
extra["coroutines"] = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
extra["coreKtx"] = "androidx.core:core-ktx:$core"
extra["constraintlayout"] = "androidx.constraintlayout:constraintlayout:$constraintLayout"
