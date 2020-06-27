package appdependencies

object Libs {
    object Core {
        const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
        const val coreKtx = "androidx.core:core-ktx:${Versions.appCoreX}"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
        const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}"
    }

    object Tests {
        const val junit = "junit:junit:${Versions.junit}"
        const val runner = "com.android.support.test:runner:${Versions.runner}"
        const val espresso = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    }
}