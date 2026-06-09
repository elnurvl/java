# Domain modules (`ddd/`)

The domain modules of the monolith live here:

- **`shared-kernel`** — the shared *domain* model (e.g. `Money`) that bounded contexts build upon.
  It is a dependency sink: contexts may use it, it depends on no context.
- **one folder per bounded context** — each a self-contained module.

(The `platform` module — shared *technical* plumbing with no domain meaning — and `app` — the
deployable composition root — deliberately stay at the repository root, not here.)

Everything in `ddd/` is **discovered and wired in automatically** — there is nothing to register by
hand:

- `settings.gradle.kts` includes every folder under `ddd/` that has a `build.gradle.kts`.
- `app` (the deployable assembly) depends on every module found here.

## Add a bounded context

```bash
./gradlew newContext -Pcontext=ordering
```

This scaffolds `ddd/ordering/` with a one-line build file (applying the
`io.github.elnurvl.bounded-context` convention) and the layered package skeleton enforced by
`ModuleBoundariesTest`:

```
io.github.elnurvl.ddd.ordering.domain          model; depends on nothing
                              .application      use cases; orchestrates the domain
                              .infrastructure   adapters; outermost, depended on by no one
                              .api              published contract other contexts may depend on
```

Contexts integrate only through each other's `..api..` (Open Host Service), the `shared-kernel`
(shared domain model), or the `platform` (shared technical building blocks) — never by reaching
into another context's internals.
