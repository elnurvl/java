// Convention for a bounded-context module in the modular monolith.
//
// A bounded context is a `java-library` that lives under `ddd/` (alongside the `shared-kernel`). It
// is discovered and wired into the `app` assembly automatically (see the root `settings.gradle.kts`
// and `app/build.gradle.kts`), so adding one needs no edit to any shared build file. Its packages
// follow the layered layout that `ModuleBoundariesTest` enforces:
//
//   io.github.elnurvl.ddd.<context>.domain          model; depends on nothing
//                                  .application      use cases; orchestrates the domain
//                                  .infrastructure   adapters; outermost, depended on by no one
//                                  .api              the published contract other contexts may use
//
// Scaffold a new one with `./gradlew newContext -Pcontext=<name>`.
plugins {
    id("io.github.elnurvl.java-conventions")
    `java-library`
}
