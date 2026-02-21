## 1. Core API types

- [x] 1.1 Create `Clock` functional interface with `long nanoTime()`
- [x] 1.2 Create `Result` record with `allowed`, `remainingTokens`, `retryAfter`, `limit`
- [x] 1.3 Create `Strategy` interface with `Result tryAcquire(String clientId)`
- [x] 1.4 Create `RateLimiter` class that accepts a `Strategy` and delegates `tryAcquire`

## 2. Token Bucket Strategy

- [x] 2.1 Implement `TokenBucketStrategy` with `synchronized` per-client state: construction, initial full bucket, token consumption
- [x] 2.2 Implement gradual refill based on elapsed time (whole tokens only, capped at capacity)
- [x] 2.3 Implement `retryAfter` calculation (time until next whole token)

## 3. Fixed Window Strategy

- [x] 3.1 Implement `FixedWindowStrategy` with `AtomicLong` CAS bit-packed state: construction, counter increment within window
- [x] 3.2 Implement window reset when time crosses window boundary
- [x] 3.3 Implement `retryAfter` calculation (time until window boundary)

## 4. Tests — Token Bucket

- [x] 4.1 Test initial state is full, token consumption depletes to zero, rejection when empty
- [x] 4.2 Test gradual refill: partial refill, capacity cap, sub-token elapsed time
- [x] 4.3 Test retryAfter: correct duration when rejected, Duration.ZERO when allowed
- [x] 4.4 Test per-client isolation: independent buckets for different client IDs

## 5. Tests — Fixed Window

- [x] 5.1 Test counter increments within a window, rejection at limit
- [x] 5.2 Test window reset at boundary, wall-clock-aligned windows shared across clients
- [x] 5.3 Test retryAfter: correct duration mid-window, Duration.ZERO when allowed
- [x] 5.4 Test per-client isolation: independent counters for different client IDs

## 6. Tests — Concurrency

- [x] 6.1 Concurrent hammer test for token bucket: N threads for same client, verify exactly `capacity` allowed
- [x] 6.2 Concurrent hammer test for fixed window: N threads for same client, verify exactly `maxRequests` allowed
- [x] 6.3 Concurrent test for different clients: verify no cross-client interference under contention

## 7. Tests — RateLimiter delegation

- [x] 7.1 Test RateLimiter delegates to strategy and returns result unchanged
