package io.github.elnurvl.limiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class FixedWindowStrategyTest {

  private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
  private static final long ONE_MINUTE_NANOS = ONE_MINUTE.toNanos();

  @Test
  void firstRequestIsAllowed() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(10, ONE_MINUTE, clock::get);

    Result result = strategy.tryAcquire("client-1");

    assertThat(result.allowed()).isTrue();
    assertThat(result.remainingTokens()).isEqualTo(9);
    assertThat(result.limit()).isEqualTo(10);
  }

  @Test
  void requestsAllowedUpToLimit() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(3, ONE_MINUTE, clock::get);

    assertThat(strategy.tryAcquire("c").remainingTokens()).isEqualTo(2);
    assertThat(strategy.tryAcquire("c").remainingTokens()).isEqualTo(1);
    Result third = strategy.tryAcquire("c");
    assertThat(third.allowed()).isTrue();
    assertThat(third.remainingTokens()).isEqualTo(0);
  }

  @Test
  void rejectedAtLimit() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(3, ONE_MINUTE, clock::get);

    for (int i = 0; i < 3; i++) {
      strategy.tryAcquire("c");
    }
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isFalse();
    assertThat(result.remainingTokens()).isEqualTo(0);
  }

  @Test
  void counterResetsAtWindowBoundary() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(3, ONE_MINUTE, clock::get);

    for (int i = 0; i < 3; i++) {
      strategy.tryAcquire("c");
    }
    assertThat(strategy.tryAcquire("c").allowed()).isFalse();

    // Advance past the window boundary
    clock.set(2 * ONE_MINUTE_NANOS);
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isTrue();
    assertThat(result.remainingTokens()).isEqualTo(2);
  }

  @Test
  void wallClockAlignedWindowsSharedAcrossClients() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS + 10_000_000_000L); // 10s into window
    var strategy = new FixedWindowStrategy(10, ONE_MINUTE, clock::get);

    strategy.tryAcquire("client-A");

    clock.set(ONE_MINUTE_NANOS + 30_000_000_000L); // 30s into same window
    strategy.tryAcquire("client-B");

    // Both should reset at the same boundary (2 * ONE_MINUTE_NANOS)
    clock.set(2 * ONE_MINUTE_NANOS - 1); // just before boundary
    // Exhaust client-A's limit
    for (int i = 0; i < 9; i++) {
      strategy.tryAcquire("client-A");
    }
    assertThat(strategy.tryAcquire("client-A").allowed()).isFalse();

    // Cross the boundary
    clock.set(2 * ONE_MINUTE_NANOS);
    assertThat(strategy.tryAcquire("client-A").allowed()).isTrue();
    assertThat(strategy.tryAcquire("client-B").allowed()).isTrue();
  }

  @Test
  void retryAfterMidWindow() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS + 20_000_000_000L); // 20s into window
    var strategy = new FixedWindowStrategy(1, ONE_MINUTE, clock::get);

    strategy.tryAcquire("c");
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isFalse();
    assertThat(result.retryAfter()).isEqualTo(Duration.ofSeconds(40));
  }

  @Test
  void retryAfterIsZeroWhenAllowed() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(10, ONE_MINUTE, clock::get);

    Result result = strategy.tryAcquire("c");

    assertThat(result.retryAfter()).isEqualTo(Duration.ZERO);
  }

  @Test
  void perClientIsolation() {
    var clock = new AtomicLong(ONE_MINUTE_NANOS);
    var strategy = new FixedWindowStrategy(1, ONE_MINUTE, clock::get);

    Result resultA = strategy.tryAcquire("client-A");
    Result resultB = strategy.tryAcquire("client-B");

    assertThat(resultA.allowed()).isTrue();
    assertThat(resultB.allowed()).isTrue();
  }
}
