import java.util.Properties
import java.io.FileInputStream


plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.syu.itzy_mayo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.syu.itzy_mayo"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "NCP_CLIENT_ID",
                "\"${localProperties.getProperty("NCP_CLIENT_ID", "")}\""
        )
        buildConfigField("String", "NAVER_API_CLIENT_ID",
                "\"${localProperties.getProperty("NAVER_API_CLIENT_ID", "")}\""
        )
        buildConfigField("String", "NAVER_API_CLIENT_SECRET",
                "\"${localProperties.getProperty("NAVER_API_CLIENT_SECRET", "")}\""
        )
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navermapsdk)
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")


    // Navigation Component (추가)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}