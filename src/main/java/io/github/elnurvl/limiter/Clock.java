package io.github.elnurvl.limiter;

/** Time source abstraction for rate limiting strategies. */
@FunctionalInterface
public interface Clock {
  /** Returns the current time in nanoseconds. */
  long nanoTime();
}
