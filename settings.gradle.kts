rootProject.name = "java"

// Modular monolith: one deployable assembly (`app`) composed of independent modules.
// `platform` holds shared *technical* building blocks (e.g. event plumbing) and stays at the root
// because it carries no domain meaning; the domain modules live under `ddd/` (see below).
include("platform")
include("app")

// `ddd/` holds the domain modules — every bounded context plus the `shared-kernel` (shared domain
// model, e.g. Money). They are discovered and included automatically: drop a folder there (e.g. via
// `./gradlew newContext -Pcontext=ordering`) and it is wired into the build with no edit here —
// `app` then depends on it automatically too.
rootDir.resolve("ddd").listFiles()
    ?.filter { it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { dir ->
        include(dir.name)
        project(":${dir.name}").projectDir = dir
    }
