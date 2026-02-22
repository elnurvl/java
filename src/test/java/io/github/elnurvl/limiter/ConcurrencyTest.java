package io.github.elnurvl.limiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ConcurrencyTest {

  @Test
  void tokenBucketHammerSameClient() throws InterruptedException {
    int capacity = 100;
    var strategy = new TokenBucketStrategy(capacity, 1);
    int threadCount = 200;

    int allowed = hammerSameClient(strategy, "client-A", threadCount);

    assertThat(allowed).isEqualTo(capacity);
  }

  @Test
  void fixedWindowHammerSameClient() throws InterruptedException {
    int maxRequests = 100;
    var strategy = new FixedWindowStrategy(maxRequests, Duration.ofMinutes(1));
    int threadCount = 200;

    int allowed = hammerSameClient(strategy, "client-A", threadCount);

    assertThat(allowed).isEqualTo(maxRequests);
  }

  @Test
  void tokenBucketConcurrentDifferentClients() throws InterruptedException {
    int limit = 10;
    var strategy = new TokenBucketStrategy(limit, 1);
    hammerDifferentClients(strategy, limit, 10);
  }

  @Test
  void fixedWindowConcurrentDifferentClients() throws InterruptedException {
    int limit = 10;
    var strategy = new FixedWindowStrategy(limit, Duration.ofMinutes(1));
    hammerDifferentClients(strategy, limit, 10);
  }

  private int hammerSameClient(Strategy strategy, String clientId, int threadCount)
      throws InterruptedException {
    var ready = new CountDownLatch(threadCount);
    var start = new CountDownLatch(1);
    var allowed = new AtomicInteger(0);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < threadCount; i++) {
        executor.submit(
            () -> {
              ready.countDown();
              try {
                start.await();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }
              Result result = strategy.tryAcquire(clientId);
              if (result.allowed()) {
                allowed.incrementAndGet();
              }
            });
      }

      ready.await();
      start.countDown();
      executor.shutdown();
      executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    return allowed.get();
  }

  private void hammerDifferentClients(Strategy strategy, int limit, int clientCount)
      throws InterruptedException {
    int threadsPerClient = limit;
    var ready = new CountDownLatch(clientCount * threadsPerClient);
    var start = new CountDownLatch(1);
    var allowed = new AtomicInteger[clientCount];
    for (int i = 0; i < clientCount; i++) {
      allowed[i] = new AtomicInteger(0);
    }

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int c = 0; c < clientCount; c++) {
        String clientId = "client-" + c;
        int clientIndex = c;
        for (int t = 0; t < threadsPerClient; t++) {
          executor.submit(
              () -> {
                ready.countDown();
                try {
                  start.await();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return;
                }
                Result result = strategy.tryAcquire(clientId);
                if (result.allowed()) {
                  allowed[clientIndex].incrementAndGet();
                }
              });
        }
      }

      ready.await();
      start.countDown();
      executor.shutdown();
      executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    for (int c = 0; c < clientCount; c++) {
      assertThat(allowed[c].get())
          .as("client-%d should have exactly %d allowed", c, limit)
          .isEqualTo(limit);
    }
  }
}
