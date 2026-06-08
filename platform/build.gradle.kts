// Shared technical building blocks (event plumbing, etc.) reused across contexts.
// Distinct from `shared-kernel`, which holds the shared *domain* model — this module carries no
// business meaning, only cross-cutting infrastructure abstractions.
plugins {
    id("io.github.elnurvl.java-conventions")
    `java-library`
}
