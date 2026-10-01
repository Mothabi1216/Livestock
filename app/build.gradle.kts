import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val emailSecrets = Properties().apply {
    val secretsFile = rootProject.file("email-secrets.properties")
    if (secretsFile.exists()) secretsFile.inputStream().use { load(it) }
}

fun emailConfigString(name: String): String {
    val value = emailSecrets.getProperty(name, "")
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "\\r") + "\""
}

android {
    namespace = "com.example.livestock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.livestock"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SMTP_EMAIL", emailConfigString("SMTP_EMAIL"))
        buildConfigField("String", "SMTP_PASSWORD", emailConfigString("SMTP_PASSWORD"))
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
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            // JavaMail often has conflicting meta-inf files
            merges += "META-INF/mailcap"
            merges += "META-INF/mimetypes.default"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.mpandroidchart)
    
    // DIRECT EMAIL SENDING (JavaMail)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    
    // OSMDROID (OpenStreetMap)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // LOCATION SERVICES
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // GLIDE FOR ROBUST IMAGE LOADING
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
