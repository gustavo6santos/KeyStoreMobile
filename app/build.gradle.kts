plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android") version "2.44"
    id("kotlin-kapt")
}




android {
    namespace = "pt.ipca.keystore"
    compileSdk = 34

    defaultConfig {
        applicationId = "pt.ipca.keystore"
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


    //loading button
    implementation (libs.loading.button.android)

    //Glide
    implementation (libs.glide)

    //circular image
    implementation (libs.circleimageview)

    //viewpager2 indicator
    implementation (libs.viewpagerindicator)

    //stepView
    implementation (libs.stepview)

    //Android Ktx
    implementation (libs.androidx.navigation.fragment.ktx)

    //Dagger hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)


}