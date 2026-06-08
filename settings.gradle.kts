rootProject.name = "java"

// Modular monolith: one deployable assembly (`app`) composed of independent modules.
// Each bounded context is its own module; `shared-kernel` holds the shared *domain* model
// (e.g. Money) and `platform` holds shared *technical* building blocks (e.g. event plumbing).
include("shared-kernel")
include("platform")
include("app")
