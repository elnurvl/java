package io.github.elnurvl.limiter;

/** Rate limiter that delegates to a pluggable {@link Strategy}. */
public final class RateLimiter {

  private final Strategy strategy;

  /** Creates a rate limiter that delegates to the given strategy. */
  public RateLimiter(Strategy strategy) {
    this.strategy = strategy;
  }

  /** Attempts to acquire permission for the given client. */
  public Result tryAcquire(String clientId) {
    return strategy.tryAcquire(clientId);
  }
}
