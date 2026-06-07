// Hosts the shared convention plugin(s) applied by every module.
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // Lets the convention plugin apply Spotless by id via its plugins {} block.
    implementation(libs.spotless.gradle.plugin)
}
