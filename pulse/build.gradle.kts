plugins {
    alias(libs.plugins.android.library)
    // 1. Tambahkan plugin Compose Compiler di sini
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.publish)
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = "io.github.otangid",
        artifactId = "PulseCompose",
        version = "1.0"
    )

    pom {
        name.set("PulseCompose Library")
        description.set("A library for visualized audio wave form.")
        inceptionYear.set("2026")
        url.set("https://github.com/otangid/PulseCompose")

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("otangid")
                name.set("Diki Zulkarnaen")
                email.set("dikizulkarnaen021@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/otangid/PulseCompose")
            connection.set("scm:git:git://github.com/otangid/PulseCompose.git")
            developerConnection.set("scm:git:ssh://github.com/otangid/PulseCompose.git")
        }
    }
}