buildscript {
    dependencies {
        classpath (libs.hilt.android.gradle.plugin)
        classpath (libs.google.services)

        classpath (libs.androidx.navigation.safe.args.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false

}