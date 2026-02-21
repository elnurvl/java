package io.github.elnurvl.limiter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

  @Test
  void delegatesToStrategyAndReturnsResultUnchanged() {
    Strategy strategy = mock(Strategy.class);
    var expected = new Result(true, 7, Duration.ZERO, 10);
    when(strategy.tryAcquire("client-1")).thenReturn(expected);

    var limiter = new RateLimiter(strategy);
    Result result = limiter.tryAcquire("client-1");

    assertThat(result).isSameAs(expected);
    verify(strategy).tryAcquire("client-1");
  }
}
