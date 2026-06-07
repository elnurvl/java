rootProject.name = "java"

// Modular monolith: one deployable assembly (`app`) composed of independent modules.
// Each bounded context is its own module; `shared-kernel` holds shared building blocks (e.g. Money).
include("shared-kernel")
include("app")
