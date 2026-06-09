// Deployable assembly: the composition root that wires the modules into one monolith.
plugins {
    id("io.github.elnurvl.java-conventions")
    application
}

application {
    mainClass = "io.github.elnurvl.ddd.Main"
}

dependencies {
    implementation(project(":platform"))

    // Every domain module under `ddd/` (the bounded contexts and the shared kernel) is part of the
    // deployable assembly, so they are wired in automatically — adding a context needs no edit here.
    val dddRoot = rootDir.resolve("ddd")
    rootProject.subprojects
        .filter { it.projectDir.parentFile == dddRoot }
        .forEach { implementation(project(it.path)) }
}
