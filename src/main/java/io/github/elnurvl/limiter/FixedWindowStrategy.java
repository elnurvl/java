package io.github.elnurvl.limiter;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/** Fixed window rate limiting with wall-clock-aligned windows and CAS-based concurrency. */
public final class FixedWindowStrategy implements Strategy {

  private final int maxRequests;
  private final long windowDurationNanos;
  private final Clock clock;
  private final ConcurrentMap<String, AtomicReference<Window>> windows = new ConcurrentHashMap<>();

  /** Creates a fixed window strategy with the given request limit and window duration. */
  public FixedWindowStrategy(int maxRequests, Duration windowDuration) {
    this(maxRequests, windowDuration, System::nanoTime);
  }

  /** Creates a fixed window strategy with the given request limit and window duration. */
  public FixedWindowStrategy(int maxRequests, Duration windowDuration, Clock clock) {
    this.maxRequests = maxRequests;
    this.windowDurationNanos = windowDuration.toNanos();
    this.clock = clock;
  }

  @Override
  public Result tryAcquire(String clientId) {
    AtomicReference<Window> ref =
        windows.computeIfAbsent(clientId, k -> new AtomicReference<>(new Window(0, 0)));
    long now = clock.nanoTime();
    long currentWindow = now / windowDurationNanos;

    while (true) {
      Window current = ref.get();

      if (current.windowId() != currentWindow) {
        var next = new Window(currentWindow, 1);
        if (ref.compareAndSet(current, next)) {
          return new Result(true, maxRequests - 1, Duration.ZERO, maxRequests);
        }
      } else if (current.counter() < maxRequests) {
        var next = new Window(currentWindow, current.counter() + 1);
        if (ref.compareAndSet(current, next)) {
          return new Result(true, maxRequests - current.counter() - 1, Duration.ZERO, maxRequests);
        }
      } else {
        long windowEndNanos = (currentWindow + 1) * windowDurationNanos;
        Duration retryAfter = Duration.ofNanos(windowEndNanos - now);
        return new Result(false, 0, retryAfter, maxRequests);
      }
    }
  }

  private record Window(long windowId, int counter) {}
}
