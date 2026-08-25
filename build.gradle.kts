import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.compose") version "1.10.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // ICS calendar parsing
    implementation("org.mnode.ical4j:ical4j:3.2.14")
    implementation("org.slf4j:slf4j-nop:2.0.16") // suppress ical4j logging noise

    // JSON parsing
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "NextMeeting"
            packageVersion = "1.0.0"
            macOS {
                bundleID = "com.example.nextmeeting"
            }
        }
    }
}
