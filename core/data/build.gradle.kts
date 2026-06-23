import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Client-safe config (Supabase anon key is a public client key by design). Sourced from
// local.properties (gitignored) or env; empty defaults keep CI/offline builds green.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun cfg(key: String): String = localProps.getProperty(key) ?: System.getenv(key) ?: ""

android {
    namespace = "ai.algo1.marketbasket.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "SUPABASE_URL", "\"${cfg("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${cfg("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "BACKEND_BASE_URL", "\"${cfg("BACKEND_BASE_URL")}\"")
        buildConfigField("String", "WHATSAPP_NUMBER", "\"${cfg("WHATSAPP_NUMBER")}\"")
        buildConfigField("String", "CATALOG_BASE_URL", "\"${cfg("CATALOG_BASE_URL")}\"")
        buildConfigField("String", "CATALOG_API_KEY", "\"${cfg("CATALOG_API_KEY")}\"")
        buildConfigField("String", "CATALOG_STORE_URN", "\"${cfg("CATALOG_STORE_URN")}\"")
        buildConfigField("String", "CATALOG_DEVICE_ID", "\"${cfg("CATALOG_DEVICE_ID")}\"")
        buildConfigField("String", "CATALOG_PARTNER_URN", "\"${cfg("CATALOG_PARTNER_URN")}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    // The supabase BOM governs the postgrest/realtime versions (declared without a version.ref).
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
