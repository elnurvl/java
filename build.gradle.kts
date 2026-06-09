// The cross-module aggregation API (reporting/reports, JacocoCoverageReport, jacocoAggregation)
// is still @Incubating in Gradle; it is the sanctioned way to merge coverage, so opt in knowingly.
@file:Suppress("UnstableApiUsage")

plugins {
    // Gives the aggregation root a `clean` task / lifecycle.
    base
    // Merges per-module coverage into a single project-wide report.
    `jacoco-report-aggregation`
}

// The aggregated report's JaCoCo tooling is resolved against the root project.
repositories {
    mavenCentral()
}

// Pull coverage data from every module into the aggregated report.
dependencies {
    subprojects.forEach { jacocoAggregation(it) }
}

reporting {
    reports {
        register<JacocoCoverageReport>("testCodeCoverageReport") {
            testSuiteName = "test"
        }
    }
}

// The coverage gate and the CI badge generator parse the CSV, which is off by default.
tasks.named<JacocoReport>("testCodeCoverageReport") {
    reports {
        csv.required = true
    }
}

// Scaffolds a new bounded-context module: `./gradlew newContext -Pcontext=ordering`.
// Creates `ddd/<name>` with its one-line build file and the layered package skeleton that
// `ModuleBoundariesTest` expects; settings.gradle.kts then discovers it and `app` wires it in on the
// next Gradle invocation — no shared build file is touched by hand.
tasks.register("newContext") {
    group = "scaffolding"
    description = "Creates a new bounded-context module under ddd/ (use -Pcontext=<name>)."
    doLast {
        val name = (project.findProperty("context") as String?)
            ?: error("Provide the context name: ./gradlew newContext -Pcontext=ordering")
        require(name.matches(Regex("[a-z][a-z0-9]*"))) {
            "Context name must be a single lower-case package segment (got '$name')."
        }
        val moduleDir = rootDir.resolve("ddd/$name")
        require(!moduleDir.exists()) { "Context '$name' already exists at $moduleDir." }

        val basePkg = "io.github.elnurvl.ddd.$name"
        val basePkgPath = basePkg.replace('.', '/')

        moduleDir.mkdirs()
        moduleDir.resolve("build.gradle.kts").writeText(
            """
            // Bounded context: $name. Discovered by settings.gradle.kts and wired into `app` automatically.
            plugins {
                id("io.github.elnurvl.bounded-context")
            }
            """.trimIndent() + "\n",
        )

        // Layered package skeleton; each package-info.java documents that layer's role and gives the
        // architecture tests a class to anchor the package to.
        // Kept short and name-independent so google-java-format never reflows them onto more lines.
        val layers = mapOf(
            "domain" to "Domain model: entities, value objects and domain events.",
            "application" to "Use cases that orchestrate the domain.",
            "infrastructure" to "Adapters (persistence, messaging); the outermost layer.",
            "api" to "Published contract other bounded contexts may depend on.",
        )
        layers.forEach { (layer, doc) ->
            val dir = moduleDir.resolve("src/main/java/$basePkgPath/$layer")
            dir.mkdirs()
            dir.resolve("package-info.java").writeText(
                """
                /** $doc */
                package $basePkg.$layer;
                """.trimIndent() + "\n",
            )
        }
        // Mirror test source root so the module is ready for tests.
        moduleDir.resolve("src/test/java/$basePkgPath").mkdirs()

        logger.lifecycle("Created bounded context '$name' at ddd/$name.")
        logger.lifecycle("It is included and wired into `app` automatically on the next Gradle run.")
    }
}
