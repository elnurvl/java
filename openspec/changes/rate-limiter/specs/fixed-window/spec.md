## ADDED Requirements

### Requirement: Fixed window construction

`FixedWindowStrategy` SHALL accept `maxRequests` (int), `windowDuration` (Duration), and a `Clock`.

#### Scenario: First request starts the window
- **GIVEN** a fixed window with max 10 requests per 60-second window
- **WHEN** `tryAcquire("client-1")` is called for the first time
- **THEN** `allowed` is `true`, `remainingTokens` is `9`, and `limit` is `10`

### Requirement: Counter within a window

Each allowed `tryAcquire` for a given client SHALL increment that client's counter. When the counter reaches `maxRequests`, further requests SHALL be rejected until the window resets.

#### Scenario: Requests allowed up to limit
- **GIVEN** a fixed window with max 3 requests
- **WHEN** `tryAcquire` is called 3 times for the same client within the same window
- **THEN** all 3 return `allowed = true` with remaining tokens 2, 1, 0

#### Scenario: Request rejected at limit
- **GIVEN** a fixed window with max 3 requests and 3 already consumed in the current window
- **WHEN** `tryAcquire` is called again for the same client
- **THEN** `allowed` is `false` and `remainingTokens` is `0`

### Requirement: Window reset

When the current time falls into a new window (determined by `nanoTime / windowDurationNanos`), the counter for that client SHALL reset to zero.

#### Scenario: Counter resets at window boundary
- **GIVEN** a fixed window with max 3 requests and all 3 consumed
- **WHEN** time advances past the window boundary
- **THEN** the next `tryAcquire` returns `allowed = true` and `remainingTokens` is `2`

### Requirement: Fixed window retryAfter

When `allowed` is `false`, `retryAfter` SHALL indicate the time remaining until the current window ends.

#### Scenario: retryAfter mid-window
- **GIVEN** a fixed window with 60-second duration, limit reached at 20 seconds into the window
- **WHEN** `tryAcquire` is called and rejected
- **THEN** `retryAfter` is 40 seconds (time until window boundary)

#### Scenario: retryAfter when allowed
- **GIVEN** a fixed window with available capacity
- **WHEN** `tryAcquire` is called and allowed
- **THEN** `retryAfter` is `Duration.ZERO`

### Requirement: Wall-clock-aligned windows

Windows SHALL be aligned to absolute time divisions (`nanoTime / windowDurationNanos`), not anchored to individual client activity. All clients share the same window boundaries.

#### Scenario: Two clients share window boundaries
- **GIVEN** a fixed window with 60-second duration
- **WHEN** client-A makes a request at t=10s and client-B at t=30s (same window)
- **THEN** both clients' windows reset at the same boundary (t=60s)
