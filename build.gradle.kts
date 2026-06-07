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
