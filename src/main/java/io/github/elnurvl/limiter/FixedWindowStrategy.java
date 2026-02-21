package io.github.elnurvl.limiter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/** Fixed window rate limiting with wall-clock-aligned windows and CAS-based concurrency. */
public final class FixedWindowStrategy implements Strategy {

  private final int maxRequests;
  private final long windowDurationNanos;
  private final Clock clock;
  private final ConcurrentMap<String, AtomicLong> windows = new ConcurrentHashMap<>();

  /** Creates a fixed window strategy with the given request limit and window duration. */
  public FixedWindowStrategy(int maxRequests, Duration windowDuration, Clock clock) {
    this.maxRequests = maxRequests;
    this.windowDurationNanos = windowDuration.toNanos();
    this.clock = clock;
  }

  @Override
  public Result tryAcquire(String clientId) {
    AtomicLong state = windows.computeIfAbsent(clientId, k -> new AtomicLong(0));
    long now = clock.nanoTime();
    long currentWindow = now / windowDurationNanos;

    while (true) {
      long current = state.get();
      int windowId = (int) (current >>> 32);
      int counter = (int) (current & 0xFFFFFFFFL);

      if (windowId != (int) currentWindow) {
        long next = (currentWindow << 32) | 1L;
        if (state.compareAndSet(current, next)) {
          return new Result(true, maxRequests - 1, Duration.ZERO, maxRequests);
        }
      } else if (counter < maxRequests) {
        long next = (currentWindow << 32) | (counter + 1L);
        if (state.compareAndSet(current, next)) {
          return new Result(true, maxRequests - counter - 1, Duration.ZERO, maxRequests);
        }
      } else {
        long windowEndNanos = (currentWindow + 1) * windowDurationNanos;
        Duration retryAfter = Duration.ofNanos(windowEndNanos - now);
        return new Result(false, 0, retryAfter, maxRequests);
      }
    }
  }
}
