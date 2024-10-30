import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "dev.psiae"
version = "v1.0.0-alpha04"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")

    implementation("io.github.vinceglb:filekit-core:0.8.7")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")

    // zip
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // rar
    implementation("com.github.junrar:junrar:7.5.5")

    // zip, rar5, 7z
    implementation("net.sf.sevenzipjbinding:sevenzipjbinding:16.02-2.01")
    implementation("net.sf.sevenzipjbinding:sevenzipjbinding-windows-amd64:16.02-2.01")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "MLToolBox"
            packageVersion = "1.0.0"

            modules("jdk.unsupported")

            windows {
                iconFile.set(project.file("src/main/resources/drawable/icon_manorlords_logo_text.ico"))
            }
        }


        buildTypes.release.proguard {

            version.set("7.6.0")
            configurationFiles.from(project.file("proguard-rules.pro"))
            isEnabled.set(true)
            obfuscate.set(true)
            joinOutputJars.set(true)
        }
    }
    dependencies {
        implementation(compose.material3)
    }
}
