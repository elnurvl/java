### Requirement: Token bucket construction

`TokenBucketStrategy` SHALL accept `capacity` (int), `refillRate` (tokens per second, int), and a `Clock`. The bucket for each client SHALL start full (tokens = capacity).

#### Scenario: Initial state is full
- **GIVEN** a token bucket with capacity 10 and refill rate 2/sec
- **WHEN** `tryAcquire("client-1")` is called immediately
- **THEN** `allowed` is `true` and `remainingTokens` is `9`

### Requirement: Token consumption

Each allowed `tryAcquire` call SHALL consume exactly one token. When no tokens remain, `tryAcquire` SHALL return `allowed = false`.

#### Scenario: Tokens deplete to zero
- **GIVEN** a token bucket with capacity 3
- **WHEN** `tryAcquire` is called 3 times for the same client
- **THEN** all 3 return `allowed = true` with remaining tokens 2, 1, 0 respectively

#### Scenario: Request rejected when empty
- **GIVEN** a token bucket with capacity 1 and the single token consumed
- **WHEN** `tryAcquire` is called again for the same client
- **THEN** `allowed` is `false` and `remainingTokens` is `0`

### Requirement: Gradual token refill

Tokens SHALL refill based on elapsed time since the last refill, at the configured refill rate. Tokens SHALL NOT exceed capacity. Only whole tokens SHALL be added.

#### Scenario: Partial refill after time elapses
- **GIVEN** a token bucket with capacity 10, refill rate 2/sec, and 0 tokens remaining
- **WHEN** 1.5 seconds elapse and `tryAcquire` is called
- **THEN** 3 tokens have been refilled (floor of 1.5 x 2), `allowed` is `true`, and `remainingTokens` is `2`

#### Scenario: Refill capped at capacity
- **GIVEN** a token bucket with capacity 5, refill rate 10/sec, and 3 tokens remaining
- **WHEN** 1 second elapses and `tryAcquire` is called
- **THEN** tokens are capped at 5 (not 13), `allowed` is `true`, and `remainingTokens` is `4`

#### Scenario: Sub-token elapsed time does not refill
- **GIVEN** a token bucket with capacity 10, refill rate 1/sec, and 0 tokens remaining
- **WHEN** 0.5 seconds elapse and `tryAcquire` is called
- **THEN** no tokens are refilled, `allowed` is `false`

### Requirement: Token bucket retryAfter

When `allowed` is `false`, `retryAfter` SHALL indicate the time until the next whole token is available based on the refill rate.

#### Scenario: retryAfter when empty
- **GIVEN** a token bucket with refill rate 2/sec and 0 tokens, last refilled 0.3 seconds ago
- **WHEN** `tryAcquire` is called and rejected
- **THEN** `retryAfter` is 0.2 seconds (time until elapsed reaches 0.5s for 1 token at 2/sec)

#### Scenario: retryAfter when allowed
- **GIVEN** a token bucket with available tokens
- **WHEN** `tryAcquire` is called and allowed
- **THEN** `retryAfter` is `Duration.ZERO`
