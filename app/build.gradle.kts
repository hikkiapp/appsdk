import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.zip.ZipFile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

android {
    namespace = "hikki.sdk"
    compileSdk = 36

    defaultConfig {
        applicationId = "hikki.sdk"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("beta") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-beta-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.7")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:6.1")
}

fun registerDexExtractTask(
    variantName: String
) {
    val cap = variantName.replaceFirstChar { it.uppercase() }

    tasks.register("extract${cap}Dex") {
        dependsOn("assemble$cap")

        doLast {
            val apkDir = layout.buildDirectory
                .dir("outputs/apk/$variantName")
                .get()
                .asFile

            val apkFile = apkDir
                .listFiles()
                ?.firstOrNull { it.extension == "apk" }
                ?: error("APK not found for $variantName")

            val dexDir = File(apkDir, "dex")

            if (dexDir.exists()) {
                dexDir.deleteRecursively()
            }

            dexDir.mkdirs()

            println("APK: ${apkFile.name}")
            println("Dex output dir: ${dexDir.absolutePath}")

            ZipFile(apkFile).use { zip ->
                zip.entries().asSequence()
                    .filter { it.name.startsWith("classes") && it.name.endsWith(".dex") }
                    .forEach { entry ->
                        val outFile = File(dexDir, entry.name)
                        zip.getInputStream(entry).use { input ->
                            outFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        println("Extracted: dex/${outFile.name}")
                    }
            }
        }
    }
}

registerDexExtractTask("release")
registerDexExtractTask("beta")