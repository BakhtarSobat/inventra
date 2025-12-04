import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.kotlin

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinComposeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.resources)
                implementation(libs.koin.core)

                implementation(libs.ktor.core)
                implementation(libs.ktor.negotiation)
                implementation(libs.ktor.json)

                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                implementation(libs.coroutines.core)
                implementation(libs.datetime)

                implementation(libs.okio)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(libs.koin.test)
                implementation(libs.kotlinx.coroutines.test)
            }

        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.android)
                implementation(libs.sqldelight.android)
            }
        }
//        val iosMain by creating {
//            dependsOn(commonMain)
//            iosX64().compilations["main"].defaultSourceSet.dependsOn(this)
//            iosArm64().compilations["main"].defaultSourceSet.dependsOn(this)
//            iosSimulatorArm64().compilations["main"].defaultSourceSet.dependsOn(this)
//            dependencies {
//                implementation(libs.ktor.darwin)
//                implementation(libs.sqldelight.native)
//            }
//        }
    }
}

android {
    namespace = "com.inventra.shared"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("InventraDatabase") {
            packageName.set("com.inventra.database")
            version = 3
        }
    }
}