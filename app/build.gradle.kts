plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt") // No version specified


}

android {
    namespace = "com.example.weather"
    compileSdk = 34


    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    defaultConfig {
        applicationId = "com.example.weather"
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

val junitVersion = "4.13.2"
val hamcrestVersion = "2.2"
val archTestingVersion = "2.1.0"
val robolectricVersion = "4.8"
val androidXTestCoreVersion = "1.4.0"
val androidXTestExtKotlinRunnerVersion = "1.1.5"
val espressoVersion = "3.4.0"
val coroutinesVersion = "1.6.4"

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //Picaso
    implementation ("com.squareup.picasso:picasso:2.8")
    //Room
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")
    implementation(libs.play.services.maps)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    kapt("androidx.room:room-compiler:2.6.1")

    // Coroutines for asynchronous tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Lifecycle dependencies for ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")

    //google services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:3.1.0")


    //Navigation component
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.0")

    //Card
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.11")

    implementation ("com.wdullaer:materialdatetimepicker:4.2.3")
    implementation("com.airbnb.android:lottie:6.0.0")

    // Unit testing
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.hamcrest:hamcrest-library:2.2")
    testImplementation("androidx.arch.core:core-testing:$archTestingVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    // AndroidX Test - JVM testing
    testImplementation("androidx.test:core-ktx:$androidXTestCoreVersion")

    // AndroidX Test - Instrumented testing
    androidTestImplementation("androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("androidx.arch.core:core-testing:$archTestingVersion")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}