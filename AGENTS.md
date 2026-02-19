## Project Overview

Java 25+ experimentation repository. The main branch is an empty template; individual projects live in their own branches.

## Commands
```bash
./gradlew build                         # Build and run tests
./gradlew test                          # Run all tests
./gradlew test --tests 'io.github.elnurvl.branchName.SomeTest.methodName'  # Run a single test
./gradlew test jacocoTestReport         # Generates a coverage report at ./build/reports/jacoco/test/html
./gradlew spotlessApply                 # Auto-fix formatting
./gradlew spotlessCheck                 # Check without fixing (useful for CI)
./gradlew checkstyleMain checkstyleTest # Validates the codebase against Google Java Style Guide
./gradlew run                           # Run the application
```

## Architecture

- **Build system:** Gradle 9.3.1 with `application` plugin
- **Main class:** `io.github.elnurvl.branchName.Main`
- **Base package:** `io.github.elnurvl.branchName`
- **Test stack:** JUnit 5 + Mockito + AssertJ
- **Dependency versions:** centralized in `gradle.properties`

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