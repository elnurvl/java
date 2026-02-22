package io.github.elnurvl.limiter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Token bucket rate limiting with gradual refill and per-client state. */
public final class TokenBucketStrategy implements Strategy {

  private static final long NANOS_PER_SECOND = 1_000_000_000L;

  private final int capacity;
  private final int refillRate;
  private final Clock clock;
  private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

  /** Creates a token bucket strategy with the given capacity and refill rate (tokens/sec). */
  public TokenBucketStrategy(int capacity, int refillRate) {
    this(capacity, refillRate, System::nanoTime);
  }

  /** Creates a token bucket strategy with the given capacity and refill rate (tokens/sec). */
  public TokenBucketStrategy(int capacity, int refillRate, Clock clock) {
    this.capacity = capacity;
    this.refillRate = refillRate;
    this.clock = clock;
  }

  @Override
  public Result tryAcquire(String clientId) {
    Bucket bucket = buckets.computeIfAbsent(clientId, k -> new Bucket(capacity, clock.nanoTime()));
    return bucket.tryConsume(clock.nanoTime());
  }

  private final class Bucket {
    private int tokens;
    private long lastRefillNanos;

    Bucket(int tokens, long lastRefillNanos) {
      this.tokens = tokens;
      this.lastRefillNanos = lastRefillNanos;
    }

    synchronized Result tryConsume(long now) {
      refill(now);
      if (tokens > 0) {
        tokens--;
        return new Result(true, tokens, Duration.ZERO, capacity);
      }
      return new Result(false, 0, timeUntilNextToken(now), capacity);
    }

    private void refill(long now) {
      long elapsed = now - lastRefillNanos;
      int newTokens = (int) (elapsed * refillRate / NANOS_PER_SECOND);
      if (newTokens > 0) {
        tokens = Math.min(capacity, tokens + newTokens);
        lastRefillNanos += newTokens * NANOS_PER_SECOND / refillRate;
      }
    }

    private Duration timeUntilNextToken(long now) {
      long elapsed = now - lastRefillNanos;
      long nanosPerToken = NANOS_PER_SECOND / refillRate;
      long remaining = nanosPerToken - elapsed;
      return Duration.ofNanos(Math.max(0, remaining));
    }
  }
}
