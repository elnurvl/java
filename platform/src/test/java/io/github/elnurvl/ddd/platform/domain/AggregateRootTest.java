package io.github.elnurvl.ddd.platform.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.elnurvl.ddd.platform.event.DomainEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link AggregateRoot}. */
class AggregateRootTest {

  private record Opened(String id) implements DomainEvent {}

  private record Closed(String id) implements DomainEvent {}

  private static final class Account extends AggregateRoot<String> {
    Account(String id) {
      super(id);
    }

    void open() {
      registerEvent(new Opened(id()));
    }

    void close() {
      registerEvent(new Closed(id()));
    }

    void recordEvent(DomainEvent event) {
      registerEvent(event);
    }
  }

  @Test
  void givenRegisteredEvents_whenPulled_thenReturnedInOrder() {
    Account account = new Account("a1");
    account.open();
    account.close();
    assertThat(account.pullDomainEvents()).containsExactly(new Opened("a1"), new Closed("a1"));
  }

  @Test
  void givenNoEvents_whenPulled_thenEmpty() {
    assertThat(new Account("a1").pullDomainEvents()).isEmpty();
  }

  @Test
  void givenEventsAlreadyPulled_whenPulledAgain_thenEmpty() {
    Account account = new Account("a1");
    account.open();
    account.pullDomainEvents();
    assertThat(account.pullDomainEvents()).isEmpty();
  }

  @Test
  void givenPulledEvents_whenModified_thenUnsupported() {
    Account account = new Account("a1");
    account.open();
    List<DomainEvent> events = account.pullDomainEvents();
    assertThatThrownBy(() -> events.add(new Closed("a1")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void givenNullEvent_whenRegistered_thenThrows() {
    assertThatNullPointerException().isThrownBy(() -> new Account("a1").recordEvent(null));
  }

  @Test
  void givenSameId_whenCompared_thenEqualAsEntity() {
    assertThat(new Account("a1")).isEqualTo(new Account("a1"));
  }
}
