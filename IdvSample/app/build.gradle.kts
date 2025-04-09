plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")   // require to add
}

android {
    namespace = "com.regula.idv.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.regula.idv.sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding=true // require to add
        dataBinding=true // require to add
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.android.vision)


    //For using DocumentReaderSDK
    //DocumentReaderSDK Core
    implementation("com.regula.documentreader.core:fullrfid:7.6+@aar") {}
    //IDV DocumentReaderSDK
    implementation("com.regula.idv:docreader:2.3.+@aar") {
        isTransitive = true
    }
    //IDV API
    implementation("com.regula.idv:api:2.3.+@aar") {
        isTransitive = true
    }

    // For using FaceSDK
    //FaceSDK Core
    implementation("com.regula.face.core:basic:6.4.+@aar") {}
    //IDV FaceSDK
    implementation("com.regula.idv:face:2.3.+@aar") {
        isTransitive = true
    }

}