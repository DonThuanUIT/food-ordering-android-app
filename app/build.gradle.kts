plugins {
    alias(libs.plugins.android.application)
}

val hasGoogleServicesConfig =
    file("google-services.json").exists() ||
        file("src/debug/google-services.json").exists() ||
        file("src/release/google-services.json").exists()

if (hasGoogleServicesConfig) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.foodorderingapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.foodorderingapp"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Lifecycle (ViewModel & LiveData)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Glide
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 1. Firebase Cloud Messaging (Sử dụng BOM để đồng bộ phiên bản tự động)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    // 2. Thư viện kết nối WebSocket STOMP
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")

    // 3. RxJava (Thư viện StompProtocolAndroid yêu cầu RxJava để chạy đa luồng)
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
}
