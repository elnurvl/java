package io.github.elnurvl.limiter;

/** Rate limiting algorithm contract. */
public interface Strategy {
  /** Attempts to acquire permission for the given client. */
  Result tryAcquire(String clientId);
}
