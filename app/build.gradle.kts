plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "com.syb.travelsphere"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.syb.travelsphere"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.properties["CLOUDINARY_CLOUD_NAME"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.properties["CLOUDINARY_API_KEY"] ?: ""}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.properties["CLOUDINARY_API_SECRET"] ?: ""}\"")
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

    buildFeatures {
        viewBinding = true

        buildConfig = true
    }
}

dependencies {
    implementation(libs.osmdroid)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)

    // for live data refresh
    implementation(libs.androidx.swiperefreshlayout)

    //cloudinary - storage
    implementation(libs.cloudinary.cloudinary.android)

    implementation(libs.picasso)
    // GeoFire
    implementation(libs.geofire.android.common)

    // Room
//    implementation(libs.androidx.room.runtime)
    implementation(libs.room.runtime) // good
    implementation(libs.androidx.preference)
    kapt(libs.androidx.room.compiler) // good
    implementation(libs.androidx.room.ktx) //good

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp for logging (optional)
    implementation(libs.okhttp)

    // Gson for JSON parsing
    implementation(libs.gson)

    // Coroutines for asynchronous operations
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Lifecycle (ViewModel and LiveData)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.play.services.location)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}