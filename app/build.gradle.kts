plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

val localProps = java.util.Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

fun getApiKey(name: String): String {
    return localProps.getProperty(name, "")
}

android {
    namespace = "com.example.adityalearn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adityalearn"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // TEMP: Gemini API key
        
        buildConfigField("String", "GEMINI_API_KEY", "\"${getApiKey("GEMINI_API_KEY")}\"")
        buildConfigField("String", "OPENAI_API_KEY", "\"${getApiKey("OPENAI_API_KEY")}\"")
        buildConfigField("String", "HUGGINGFACE_API_KEY", "\"${getApiKey("HUGGINGFACE_API_KEY")}\"")
        buildConfigField("String", "GROQ_API_KEY", "\"${getApiKey("GROQ_API_KEY")}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${getApiKey("DEEPSEEK_API_KEY")}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.3"))

    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")




    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")


    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.material:material:1.9.0")
    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.airbnb.android:lottie-compose:6.5.0")

    // AndroidX UI
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // ✅ fixed version
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Firebase (BoM manages versions)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.material:material:1.12.0")

    // Firebase UI (Authentication UI)
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // ML Kit
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:digital-ink-recognition:16.0.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.android.volley:volley:1.2.1")

    // Glide (Image Loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // BlurView (✅ correct JitPack notation)
    implementation("com.github.Dimezis:BlurView:version-3.0.0")

    // Apache POI (Excel)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // JSON
    implementation("org.json:json:20230227")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.2.0-alpha10")

    // Jetpack base libs (from your version catalog)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.coordinatorlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
