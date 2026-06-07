# Fintech DDD Demo

A demo fintech application written in pure Java 25+ that explores **Domain-Driven Design** in a
**modular monolith** — a single deployable assembled from independent, well-bounded modules.

![Java](https://img.shields.io/badge/Java-25-orange)
![Build](https://github.com/elnurvl/java/actions/workflows/ci.yml/badge.svg?branch=ddd)
![Coverage](https://github.com/elnurvl/java/blob/badges/ddd/jacoco.svg)
![License](https://img.shields.io/badge/license-MIT-blue)
![Style](https://img.shields.io/badge/style-Google%20Java-4285F4)

## Architecture

A modular monolith structured around DDD:

- Each **bounded context** is an independent Gradle module, isolated behind its own domain model.
- Contexts never depend on each other directly. Shared building blocks live in **`shared-kernel`**
  — e.g. the `Money` value object.
- The **`app`** module is the composition root: the deployable that wires the contexts together.

Dependencies point inward: `app` → bounded contexts → `shared-kernel`.

| Module                  | Type          | Responsibility                                      |
|-------------------------|---------------|-----------------------------------------------------|
| `shared-kernel`         | shared kernel | Cross-context building blocks (`Money`, …)          |
| `app`                   | assembly      | Composition root and entry point                    |
| *(bounded contexts)*    | domain        | One module per context, added as the domain grows   |

## Domain building blocks

- **`Money`** — an immutable, currency-aware value object. Non-negative by invariant, normalized to
  the currency's minor unit (2 digits for USD, 0 for JPY), with currency-safe arithmetic and a
  remainder-preserving `allocate(parts)` that splits an amount into shares without losing a cent.

## Getting started

Requires **Java 25+**.

```bash
./gradlew build   # compile and test every module
./gradlew run     # run the app
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow, code style, and git hooks.

## License

MIT — see [LICENSE](LICENSE).
