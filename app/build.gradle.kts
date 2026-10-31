// Opt-in host rendering only. No screenshot engine enters the installed application.
buildscript {
    if (gradle.startParameter.projectProperties["g9Render"] == "true") {
        repositories { google(); mavenCentral() }
        dependencies { classpath("com.android.compose.screenshot:screenshot-test-gradle-plugin:0.0.1-alpha16") }
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val g9Render = providers.gradleProperty("g9Render").orNull == "true"

android {
    if (g9Render) experimentalProperties["android.experimental.enableScreenshotTest"] = true
    namespace = "com.ahmadmol.enmath"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.ahmadmol.enmath"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        create("w1Qa") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".w1qa"
            versionNameSuffix = "-w1qa"
            matchingFallbacks += listOf("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    bundle { language { enableSplit = false } }
}

if (g9Render) apply(plugin = "com.android.compose.screenshot")

if (g9Render) {
    // AGP 8.9 exposes the SDK stub jar as bootClasspath. The current standalone renderer
    // needs Layoutlib's rewritten framework classes (_Original_Build etc.) ahead of it.
    val framework = configurations.create("g9RenderFramework")
    dependencies.add(framework.name, "com.android.tools.layoutlib:layoutlib:16.1.0-jdk17")
    tasks.matching { it.name.endsWith("ScreenshotTest") }.configureEach {
        val getter = javaClass.methods.find { it.name == "getTestEngineInput" && it.parameterCount == 0 }
        if (getter != null) {
            val input = getter.invoke(this)
            val files = input.javaClass.getMethod("getLayoutlibClassPath").invoke(input) as org.gradle.api.file.ConfigurableFileCollection
            val existing = files.from.toList()
            files.setFrom(framework, existing)
        }
    }
}

// QA-only version codes allow an isolated baseline -> final upgrade without uninstalling.
androidComponents.onVariants(androidComponents.selector().withBuildType("w1Qa")) { variant ->
    variant.outputs.forEach { output ->
        output.versionCode.set(providers.gradleProperty("w1QaVersionCode").map(String::toInt).orElse(2))
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.04.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")
    if (g9Render) add("screenshotTestImplementation", "com.android.tools.screenshot:screenshot-validation-api:0.0.1-alpha16")
}
