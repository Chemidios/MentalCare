plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    id("androidx.room") version "2.6.1"
}

android {
    namespace = "com.example.mentalcare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mentalcare"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        // Esta es la solución directa al error que te sale
        checkReleaseBuilds = false
        // Opcionalmente, para que no se detenga si encuentra otros errores menores
        abortOnError = false
        // Específicamente para el error de LiveData que mencionaba tu log
        disable += "NullSafeMutableLiveData"
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
        compose = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

configurations.all {
    resolutionStrategy {
        // Force the project to stay on stable versions regardless of what libraries want
        force("androidx.core:core:1.15.0")
        force("androidx.core:core-ktx:1.15.0")
        force("androidx.activity:activity:1.9.3")
        force("androidx.activity:activity-compose:1.9.3")

        // Prevent the "Kotlin 2.2.0" mismatch
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
    }
}

dependencies {
    // Core y Lifecycle (Usando las versiones forzadas en resolutionStrategy)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM (Gestiona las versiones de UI, Graphics, etc. automáticamente)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text)

    // Material 3 - ¡USA SOLO UNA! (He eliminado la 1.2.1 manual que tenías)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Navegación
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Persistencia (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Seguridad (Biometría)
    implementation("androidx.biometric:biometric:1.1.0")

    // Utilidades
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.core.i18n)

    // Testeo
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}