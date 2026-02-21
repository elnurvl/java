## ADDED Requirements

### Requirement: Acquire permission via RateLimiter

The `RateLimiter` class SHALL accept a `Strategy` at construction and delegate `tryAcquire(String clientId)` to it. The caller SHALL NOT interact with the `Strategy` directly after construction.

#### Scenario: Allowed request
- **GIVEN** a `RateLimiter` constructed with a strategy that has remaining capacity
- **WHEN** `tryAcquire("client-1")` is called
- **THEN** the strategy's `tryAcquire` is invoked with `"client-1"` and the result is returned

#### Scenario: Rejected request
- **GIVEN** a `RateLimiter` constructed with a strategy that has no remaining capacity
- **WHEN** `tryAcquire("client-1")` is called
- **THEN** the result has `allowed = false`

### Requirement: Result record contains rate limit metadata

`tryAcquire` SHALL return a `Result` record with the following fields:
- `allowed` (boolean): whether the request was permitted
- `remainingTokens` (int): remaining capacity after this call
- `retryAfter` (Duration): time until next token or window reset; `Duration.ZERO` when allowed
- `limit` (int): the configured maximum capacity

#### Scenario: Allowed result fields
- **GIVEN** a strategy with 5 remaining tokens out of 10
- **WHEN** `tryAcquire` is called and succeeds
- **THEN** `allowed` is `true`, `remainingTokens` is `4`, `retryAfter` is `Duration.ZERO`, and `limit` is `10`

#### Scenario: Rejected result fields
- **GIVEN** a strategy with 0 remaining tokens
- **WHEN** `tryAcquire` is called and is rejected
- **THEN** `allowed` is `false`, `remainingTokens` is `0`, `retryAfter` is greater than `Duration.ZERO`, and `limit` reflects the configured capacity

### Requirement: Strategy interface defines the rate limiting contract

The `Strategy` interface SHALL declare a single method `Result tryAcquire(String clientId)`. All strategy implementations SHALL implement this interface.

#### Scenario: Strategy is interchangeable
- **GIVEN** two different `Strategy` implementations (token bucket and fixed window)
- **WHEN** each is passed to a `RateLimiter`
- **THEN** both work through the same `tryAcquire(String clientId)` call on `RateLimiter`

### Requirement: Clock abstraction for time

A `Clock` functional interface SHALL provide `long nanoTime()`. All strategies SHALL accept a `Clock` at construction. Production code SHALL use `System::nanoTime`. Tests SHALL use a controllable clock.

#### Scenario: Controllable clock in tests
- **GIVEN** a strategy constructed with a clock returning a fixed value
- **WHEN** the clock value is advanced programmatically
- **THEN** the strategy observes the new time without real wall-clock delay

### Requirement: Per-client isolation

Each unique `clientId` SHALL have independent rate limiting state. Consuming capacity for one client SHALL NOT affect any other client.

#### Scenario: Independent clients
- **GIVEN** a strategy with a limit of 1
- **WHEN** `tryAcquire("client-A")` is called and allowed
- **THEN** `tryAcquire("client-B")` is also allowed

### Requirement: Thread safety

`tryAcquire` SHALL be safe to call concurrently from multiple threads for the same and different client IDs. The total number of allowed requests for a given client SHALL NOT exceed the configured limit.

#### Scenario: Concurrent access for the same client
- **GIVEN** a strategy with a limit of 100
- **WHEN** 200 threads each call `tryAcquire("client-A")` simultaneously
- **THEN** exactly 100 calls return `allowed = true` and 100 return `allowed = false`

#### Scenario: Concurrent access for different clients
- **GIVEN** a strategy with a limit of 10
- **WHEN** 10 threads call `tryAcquire` for 10 different client IDs simultaneously
- **THEN** all 100 calls return `allowed = true` (each client is independent)
