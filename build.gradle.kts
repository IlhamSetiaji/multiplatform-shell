plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    kotlin("plugin.serialization").apply(false)
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish") version "0.25.3" apply false
    id("org.jetbrains.kotlin.plugin.atomicfu") version "1.9.20"
}