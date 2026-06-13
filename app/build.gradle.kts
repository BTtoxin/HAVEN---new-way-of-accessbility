plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.glyphqs.kjmz"
    minSdk = 24
    targetSdk = 35
    versionCode = 5
    versionName = "1.5.0"

    val metadataFile = project.rootProject.file("metadata.json")
    var metadataVersion = "1.4.0"
    if (metadataFile.exists()) {
        val metadataText = metadataFile.readText()
        val versionRegex = "\"version\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = versionRegex.find(metadataText)
        if (match != null) {
            metadataVersion = match.groupValues[1]
        }
    }
    buildConfigField("String", "METADATA_VERSION", "\"${metadataVersion}\"")
    buildConfigField("String", "APP_VERSION_NAME", "\"${versionName}\"")
    buildConfigField("int", "APP_VERSION_CODE", "${versionCode}")

    // Sync versionName → metadata.json automatically
    val metadataJson = """
    {
      "name": "Haven",
      "description": "Nothing Phone inspired Quick Settings dashboard with Natural Tones.",
      "version": "${versionName}",
      "requestFramePermissions": [],
      "majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
    }
    """.trimIndent()
    if (metadataFile.exists()) {
        val existingContent = metadataFile.readText()
        if (!existingContent.contains("\"version\": \"${versionName}\"")) {
            metadataFile.writeText(metadataJson)
        }
    }

    val changelogFile = project.file("src/main/assets/changelog.json")
    if (changelogFile.exists()) {
        val changelogText = changelogFile.readText()
        val versionRegexForChangelog = "\"currentVersion\"\\s*:\\s*\"[^\"]+\"".toRegex()
        val updatedText = versionRegexForChangelog.replace(changelogText, "\"currentVersion\": \"v${versionName}\"")
        if (updatedText != changelogText) {
            changelogFile.writeText(updatedText)
        }
    }

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("debugConfig")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

tasks.register<Copy>("copyApk") {
    dependsOn("assembleDebug", "assembleRelease", "bundleRelease")
    from(layout.buildDirectory.dir("outputs/apk/debug")) {
        include("app-debug.apk")
    }
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
    }
    from(layout.buildDirectory.dir("outputs/bundle/release")) {
        include("app-release.aab")
        into("../bundle")
    }
    into(rootProject.layout.projectDirectory.dir("build-output/apk"))
}

// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation("androidx.compose.ui:ui-text-google-fonts:1.7.0")
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  
  // Custom requested dependencies
  implementation("androidx.work:work-runtime-ktx:2.9.0")
  implementation("androidx.glance:glance-appwidget:1.1.0")
  implementation("androidx.glance:glance-material3:1.1.0")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.biometric:biometric:1.1.0")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
