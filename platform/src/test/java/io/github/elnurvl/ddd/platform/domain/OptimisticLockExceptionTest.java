package io.github.elnurvl.ddd.platform.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests for {@link OptimisticLockException}. */
class OptimisticLockExceptionTest {

  @Test
  void givenMessage_whenCreated_thenCarriesMessage() {
    assertThat(new OptimisticLockException("stale")).hasMessage("stale");
  }

  @Test
  void givenAggregate_whenCreatedViaFactory_thenMessageNamesIdAndVersion() {
    assertThat(OptimisticLockException.forAggregate("a1", 7L))
        .hasMessage("Aggregate a1 was modified concurrently; expected version 7");
  }
}
