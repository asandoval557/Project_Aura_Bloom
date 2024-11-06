plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    //Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.project_aura_bloom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.project_aura_bloom"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    // Import Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")
    //Import Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")
    //Import Firebase CloudFirestore
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    //Import Firebase Cloud Storage
    implementation("com.google.firebase:firebase-storage:21.0.1")
    //Import play integrity
    implementation("com.google.android.play:integrity:1.4.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Added dependencies
    implementation(libs.androidx.navigation.compose)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.google.android.material:material:1.12.0")

    implementation("nl.dionsegijn:konfetti-xml:2.0.2")

    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("com.github.dhaval2404:imagepicker:2.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
