// Top-level build file where you can add configuration options common to all sub-projects/modules.


plugins {

    alias(libs.plugins.android.application) apply false

}
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("com.google.gms:google-services:4.4.0") // Firebase
    }
}