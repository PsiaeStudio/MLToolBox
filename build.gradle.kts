import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "dev.psiae"
version = "v1.0.0-alpha07"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.material)
    implementation(compose.material3)
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
            copyright = "Copyright (C) 2024 Psiae"
            licenseFile.set(project.file("LICENSE"))

            modules("jdk.unsupported", "jdk.accessibility")

            windows {
                iconFile.set(project.file("src/main/resources/drawable/icon_manorlords_logo_text.ico"))
            }

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }

        buildTypes.release.proguard {

            version.set("7.6.0")
            configurationFiles.from(project.file("proguard-rules.pro"))
            isEnabled.set(true)
            obfuscate.set(true)
            joinOutputJars.set(true)
        }
    }
}

afterEvaluate {
    tasks.withType<AbstractJPackageTask>().forEach { task ->
        if (
            task.name.startsWith("create") &&
            task.name.endsWith("Distributable")
        ) {
            task.doLast {
                project.layout.projectDirectory.dir("resources").dir("common").file("LICENSE.txt").asFile
                    .copyTo(
                        target = task.destinationDir.get().dir(task.packageName.get()).file("LICENSE.txt").asFile,
                        // we expect that the dir is empty
                        overwrite = false
                    )
            }
        }
    }
}
