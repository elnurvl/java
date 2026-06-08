import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test

// Shared build configuration for every module in the monolith.
plugins {
    java
    jacoco
    checkstyle
    id("com.diffplug.spotless")
}

// Precompiled script plugins don't get the type-safe `libs` accessor, so read the catalog directly.
val libs = the<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
}

spotless {
    java {
        googleJavaFormat()
    }
}

checkstyle {
    toolVersion = libs.findVersion("checkstyle").get().requiredVersion
    maxWarnings = 0
    configDirectory = rootProject.layout.projectDirectory.dir("config/checkstyle")
}

dependencies {
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
    testImplementation(libs.findLibrary("mockito-core").get())
    testImplementation(libs.findLibrary("mockito-junit-jupiter").get())
    testImplementation(libs.findLibrary("assertj-core").get())
    testImplementation(libs.findLibrary("archunit-junit5").get())
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
