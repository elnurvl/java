## Why

The internal service platform needs a reusable rate limiting component. Different services have different needs, so the library must support multiple strategies behind a common API. Pure in-memory, pure Java — no HTTP, no frameworks, no persistence.

## What Changes

- New `RateLimiter` class that delegates to a pluggable strategy
- `tryAcquire(String clientId)` returns a result with allowed status, remaining tokens, retry-after, and limit
- Two strategies: token bucket (gradual refill) and fixed window (wall-clock-aligned counter)
- Per-client rate limiting — each client ID tracks its own limit independently
- Thread-safe under concurrent access for same and different client IDs
- Injectable time source for deterministic testing

## Non-goals

- HTTP layer, framework integration, or REST endpoints
- Persistence or distributed rate limiting
- Sliding window or other strategies beyond token bucket and fixed window
- Per-client strategy routing (all clients share one strategy per `RateLimiter` instance)
- Automatic eviction of stale client entries

## Capabilities

### New Capabilities
- `rate-limiting`: Core rate limiter API, strategy interface, result type, and time abstraction
- `token-bucket`: Token bucket strategy with gradual token refill
- `fixed-window`: Fixed window strategy with wall-clock-aligned windows

### Modified Capabilities

_None — greenfield project._

## Impact

- New package: `io.github.elnurvl.limiter`
- No external dependencies beyond JUnit 5, Mockito, and AssertJ for testing
- No existing code affected — this is a new library on the `rate-limiter` branch
