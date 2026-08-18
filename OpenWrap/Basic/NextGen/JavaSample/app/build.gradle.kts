plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.pubmatic.openwrap.gamnextgen.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pubmatic.openwrap.gamnextgen.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.androidx.multidex)

    //To integrate GMA Next-Gen SDK
    implementation("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.2.1")

    //To integrate PubMatic SDK
    implementation("com.pubmatic.sdk:openwrap:5.+")

    //To integrate GAM Next-Gen Event Handler
    implementation("com.pubmatic.sdk:openwrap-eventhandler-gamnextgen:1.+")

    // Highly recommended to fetch the Android Advertising Id in OW SDK
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
}
