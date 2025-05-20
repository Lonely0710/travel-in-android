plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bjtu.traveler"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bjtu.traveler"
        minSdk = 24
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Lottie 动画库
    implementation("com.airbnb.android:lottie:6.3.0")

    // Bmob 后端云
    implementation ("io.github.bmob:android-sdk:4.1.0")
    implementation ("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation ("com.squareup.okhttp3:okhttp:4.8.1")
    implementation ("com.squareup.okio:okio:2.2.2")
    implementation ("com.google.code.gson:gson:2.8.5")    

    implementation ("com.google.android.material:material:1.11.0")
    
    // Glide 图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}