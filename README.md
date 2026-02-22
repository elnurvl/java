# Rate Limiter

![Java](https://img.shields.io/badge/Java-25-orange)
![Build](https://github.com/elnurvl/java/actions/workflows/ci.yml/badge.svg?branch=rate-limiter)
![Coverage](https://github.com/elnurvl/java/blob/badges/rate-limiter/jacoco.svg)
![License](https://img.shields.io/badge/license-MIT-blue)
![Style](https://img.shields.io/badge/style-Google%20Java-4285F4)

A thread-safe, in-memory rate limiting library for Java 25+. Supports multiple strategies behind a common API with per-client tracking.

_Note: This project has been developed primarily to practice the Spec-Driven Development workflow provided by OpenSpec, using Claude Code (Opus 4.6 Extended) as the AI coding agent._

## Features

- **Pluggable strategies** — Token Bucket and Fixed Window out of the box, extensible via the `Strategy` interface
- **Per-client isolation** — each client ID tracks its own limit independently
- **Thread-safe** — safe for concurrent access from multiple threads
- **Rich results** — every call returns remaining tokens, retry-after duration, and the configured limit
- **Testable** — injectable `Clock` abstraction for deterministic time control in tests
- **Zero dependencies** — pure Java, no frameworks

## Quick Start

```java
// Token Bucket: 10 tokens, refills 2 per second
var limiter = new RateLimiter(new TokenBucketStrategy(10, 2));

Result result = limiter.tryAcquire("client-1");
if (result.allowed()) {
    // process request
} else {
    // reject, retry after result.retryAfter()
}
```

## Strategies

### Token Bucket

Tokens refill gradually over time. Allows short bursts up to capacity, then smoothly rate-limits.

```java
// 100 tokens max, refills 10 per second
var limiter = new RateLimiter(new TokenBucketStrategy(100, 10));
var limiterWithCustomClock = new RateLimiter(new TokenBucketStrategy(100, 10, System::nanoTime));
```

| Parameter    | Description                          |
|--------------|--------------------------------------|
| `capacity`   | Maximum tokens in the bucket         |
| `refillRate` | Tokens added per second              |
| `clock`      | Time source (`System::nanoTime`)     |

Each client starts with a full bucket. Tokens refill based on elapsed time (whole tokens only, capped at capacity). When empty, `retryAfter` indicates the time until the next token.

### Fixed Window

Counts requests within wall-clock-aligned time windows. Simple and predictable.

```java
// 1000 requests per minute
var limiter = new RateLimiter(
    new FixedWindowStrategy(1000, Duration.ofMinutes(1))
);
// 1000 requests per minute
var limiterWithCustomClock = new RateLimiter(
    new FixedWindowStrategy(1000, Duration.ofMinutes(1), System::nanoTime)
);
```

| Parameter        | Description                          |
|------------------|--------------------------------------|
| `maxRequests`    | Maximum requests per window          |
| `windowDuration` | Length of each time window            |
| `clock`          | Time source (`System::nanoTime`)     |

Windows are aligned to absolute time divisions — all clients share the same window boundaries. When the limit is hit, `retryAfter` indicates the time until the next window.

> **Note:** Clients can send up to 2x the limit across a window boundary. If you need smoother rate limiting, use Token Bucket.

## Result

Every `tryAcquire` call returns a `Result` record:

```java
record Result(
    boolean allowed,        // was the request permitted?
    int remainingTokens,    // capacity left after this call
    Duration retryAfter,    // time to wait (Duration.ZERO if allowed)
    int limit               // configured maximum capacity
)
```

Example usage:

```java
Result result = limiter.tryAcquire("client-42");

if (!result.allowed()) {
    response.setHeader("Retry-After",
        String.valueOf(result.retryAfter().toSeconds()));
    response.setHeader("X-RateLimit-Remaining",
        String.valueOf(result.remainingTokens()));
    response.setHeader("X-RateLimit-Limit",
        String.valueOf(result.limit()));
    return 429;
}
```

## Custom Strategies

Implement the `Strategy` interface to add your own algorithm:

```java
public class SlidingWindowStrategy implements Strategy {
    @Override
    public Result tryAcquire(String clientId) {
        // your logic here
    }
}

var limiter = new RateLimiter(new SlidingWindowStrategy(...));
```

## Testing

The `Clock` functional interface makes tests deterministic — no `Thread.sleep()` needed:

```java
var clock = new AtomicLong(0);
var strategy = new TokenBucketStrategy(10, 2, clock::get);

// Consume all tokens
for (int i = 0; i < 10; i++) {
    strategy.tryAcquire("client");
}

// Advance time by 1.5 seconds — 3 tokens refill
clock.set(1_500_000_000L);
Result result = strategy.tryAcquire("client");
assert result.allowed();              // true
assert result.remainingTokens() == 2; // 3 refilled, 1 consumed
```

Requires Java 25+.
