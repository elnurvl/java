## Project Overview

Java 25+ experimentation repository. The main branch is an empty template; individual projects live in their own branches.

## Commands
```bash
./gradlew build                         # Build and run tests for every module
./gradlew test                          # Run all tests
./gradlew :shared-kernel:test --tests 'io.github.elnurvl.branchName.shared.SomeTest.methodName'  # Run a single test (scope to the owning module)
./gradlew testCodeCoverageReport        # Aggregated coverage report across all modules at ./build/reports/jacoco/testCodeCoverageReport/html
./gradlew spotlessApply                 # Auto-fix formatting
./gradlew spotlessCheck                 # Check without fixing (useful for CI)
./gradlew checkstyleMain checkstyleTest # Validates the codebase against Google Java Style Guide
./gradlew run                           # Run the application (the `app` module)
```

## Architecture

This branch is a **modular monolith** following Domain-Driven Design: a single deployable
assembly composed of independent Gradle modules.

- **Build system:** Gradle 9.3.1, multi-module. Shared module config lives in a convention
  plugin (`buildSrc/src/main/groovy/io.github.elnurvl.java-conventions.gradle`) that each module
  applies; the root `build.gradle` only owns aggregated coverage (`jacoco-report-aggregation`).
- **Modules:**
  - `shared-kernel` — shared kernel (`java-library`) with building blocks reused across contexts,
    e.g. `Money`. Bounded contexts may depend on it; it depends on no context.
  - `app` — the deployable assembly (`application` plugin) and composition root; depends on
    `shared-kernel` and on each bounded-context module.
  - Each **bounded context** is added as its own module and wired in through `app`.
- **Main class:** `io.github.elnurvl.branchName.Main` (in the `app` module)
- **Base package:** `io.github.elnurvl.branchName`; the shared kernel lives under
  `io.github.elnurvl.branchName.shared`
- **Test stack:** JUnit 5 + Mockito + AssertJ
- **Dependency versions:** centralized in the version catalog (`gradle/libs.versions.toml`)

## Code Style

- Use Java 25+ features where appropriate (records, sealed classes, pattern matching, etc.)
- Avoid using frameworks
- Always follow Google Java Style Guide and enforce this with Checkstyle
- Prefer immutable data structures and records over mutable POJOs
- Write tests for all new functionality

## Commit Convention

Follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

Types: `build`, `chore`, `ci`, `docs`, `feat`, `fix`, `perf`, `refactor`, `revert`, `style`, `test`.

Format: `<type>(<optional scope>): <description>`

## Workflow

1. Run `./gradlew build` before committing to ensure tests pass
2. Test coverage must be at least 80%
3. There must be no checkstyle issue
4. Keep commits atomic and well-scoped
5. Do not modify the `main` branch structure — it is a template