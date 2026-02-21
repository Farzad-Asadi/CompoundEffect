plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.com.google.devtools.ksp)
    kotlin("kapt")
}

android {
    namespace = "com.example.compoundeffectV1_01"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.compoundeffectV1_01"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations.matching {
    it.name.contains("kapt", ignoreCase = true) ||
            it.name.contains("ksp", ignoreCase = true) ||
            it.name.contains("annotationProcessor", ignoreCase = true)
}.configureEach {
    resolutionStrategy.force("com.squareup:javapoet:1.13.0")
}

configurations.all {
    resolutionStrategy.force(
        "org.jetbrains.kotlin:kotlin-stdlib:1.9.24",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.24",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.24",
    )
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.12.0")



    implementation(libs.kotlinx.coroutines.core)





    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("androidx.navigation:navigation-compose:${rootProject.extra["nav_version"]}")



    //Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)



    //Icons
    implementation("androidx.compose.material:material-icons-extended")


    //hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //lazy Reorderable
    implementation(libs.reorderable)



    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("com.squareup:javapoet:1.13.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")


    implementation ("com.github.hamooo90:jalali-datepicker-compose:1.2.0")
    implementation ("ir.huri:JalaliCalendar:1.3.3")
    

}