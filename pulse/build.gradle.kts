plugins {
    alias(libs.plugins.android.library)
    // 1. Tambahkan plugin Compose Compiler di sini
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "otang.id.lib.pulse"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 2. Aktifkan fitur Jetpack Compose untuk modul ini
    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)

    // 3. Tambahkan dependensi UI & Tooling jika belum ada (opsional tapi disarankan)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}