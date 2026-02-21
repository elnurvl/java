package io.github.elnurvl.limiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class TokenBucketStrategyTest {

  @Test
  void initialStateIsFull() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(10, 2, clock::get);

    Result result = strategy.tryAcquire("client-1");

    assertThat(result.allowed()).isTrue();
    assertThat(result.remainingTokens()).isEqualTo(9);
    assertThat(result.limit()).isEqualTo(10);
  }

  @Test
  void tokensDepleteToZero() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(3, 1, clock::get);

    assertThat(strategy.tryAcquire("c").remainingTokens()).isEqualTo(2);
    assertThat(strategy.tryAcquire("c").remainingTokens()).isEqualTo(1);
    Result third = strategy.tryAcquire("c");
    assertThat(third.allowed()).isTrue();
    assertThat(third.remainingTokens()).isEqualTo(0);
  }

  @Test
  void rejectedWhenEmpty() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(1, 1, clock::get);

    strategy.tryAcquire("c");
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isFalse();
    assertThat(result.remainingTokens()).isEqualTo(0);
  }

  @Test
  void partialRefillAfterTimeElapses() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(10, 2, clock::get);

    // Exhaust all tokens
    for (int i = 0; i < 10; i++) {
      strategy.tryAcquire("c");
    }
    assertThat(strategy.tryAcquire("c").allowed()).isFalse();

    // Advance 1.5 seconds → 3 tokens refilled (floor of 1.5 * 2)
    clock.set(1_500_000_000L);
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isTrue();
    assertThat(result.remainingTokens()).isEqualTo(2);
  }

  @Test
  void refillCappedAtCapacity() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(5, 10, clock::get);

    // Consume 2 tokens, leaving 3
    strategy.tryAcquire("c");
    strategy.tryAcquire("c");

    // Advance 1 second → would add 10 tokens, but cap at 5
    clock.set(1_000_000_000L);
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isTrue();
    assertThat(result.remainingTokens()).isEqualTo(4);
  }

  @Test
  void subTokenElapsedTimeDoesNotRefill() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(10, 1, clock::get);

    // Exhaust all tokens
    for (int i = 0; i < 10; i++) {
      strategy.tryAcquire("c");
    }

    // Advance 0.5 seconds → 0 tokens at 1/sec
    clock.set(500_000_000L);
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isFalse();
  }

  @Test
  void retryAfterWhenRejected() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(1, 2, clock::get);

    strategy.tryAcquire("c");

    // Advance 0.3 seconds into the refill interval
    clock.set(300_000_000L);
    Result result = strategy.tryAcquire("c");

    assertThat(result.allowed()).isFalse();
    assertThat(result.retryAfter()).isEqualTo(Duration.ofMillis(200));
  }

  @Test
  void retryAfterIsZeroWhenAllowed() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(10, 1, clock::get);

    Result result = strategy.tryAcquire("c");

    assertThat(result.retryAfter()).isEqualTo(Duration.ZERO);
  }

  @Test
  void perClientIsolation() {
    var clock = new AtomicLong(0);
    var strategy = new TokenBucketStrategy(1, 1, clock::get);

    Result resultA = strategy.tryAcquire("client-A");
    Result resultB = strategy.tryAcquire("client-B");

    assertThat(resultA.allowed()).isTrue();
    assertThat(resultB.allowed()).isTrue();
  }
}
