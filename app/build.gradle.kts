plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.soundtraining"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.soundtraining"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.PL"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath:   String by project
            val keystorePass:   String by project
            val releaseAlias:   String by project
            val releaseKeyPass: String by project

            storeFile     = file(keystorePath)
            storePassword = keystorePass
            keyAlias      = releaseAlias
            keyPassword   = releaseKeyPass
        }
    }

    buildTypes {
        release {
            signingConfig   = signingConfigs["release"]
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
