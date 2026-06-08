package io.github.elnurvl.ddd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import io.github.elnurvl.ddd.platform.event.EventHandler;
import io.github.elnurvl.ddd.platform.event.IntegrationEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link InProcessEventBus}. */
class InProcessEventBusTest {

  private record Created(String name) implements IntegrationEvent {}

  private record Deleted(String name) implements IntegrationEvent {}

  private static final class Recorder<E extends IntegrationEvent> implements EventHandler<E> {
    private final List<E> received = new ArrayList<>();

    @Override
    public void handle(E event) {
      received.add(event);
    }
  }

  @Test
  void givenSubscribedHandler_whenMatchingEventPublished_thenHandlerReceivesIt() {
    InProcessEventBus bus = new InProcessEventBus();
    Recorder<Created> recorder = new Recorder<>();
    bus.subscribe(Created.class, recorder);

    bus.publish(new Created("order"));

    assertThat(recorder.received).containsExactly(new Created("order"));
  }

  @Test
  void givenMultipleHandlers_whenEventPublished_thenAllInvokedInSubscriptionOrder() {
    InProcessEventBus bus = new InProcessEventBus();
    List<String> calls = new ArrayList<>();
    bus.subscribe(Created.class, event -> calls.add("first"));
    bus.subscribe(Created.class, event -> calls.add("second"));

    bus.publish(new Created("order"));

    assertThat(calls).containsExactly("first", "second");
  }

  @Test
  void givenHandlerForAnotherType_whenEventPublished_thenNotInvoked() {
    InProcessEventBus bus = new InProcessEventBus();
    Recorder<Deleted> recorder = new Recorder<>();
    bus.subscribe(Deleted.class, recorder);

    bus.publish(new Created("order"));

    assertThat(recorder.received).isEmpty();
  }

  @Test
  void givenHandlersForDifferentTypes_whenEventsPublished_thenEachRoutedToItsType() {
    InProcessEventBus bus = new InProcessEventBus();
    Recorder<Created> created = new Recorder<>();
    Recorder<Deleted> deleted = new Recorder<>();
    bus.subscribe(Created.class, created);
    bus.subscribe(Deleted.class, deleted);

    bus.publish(new Created("a"));
    bus.publish(new Deleted("b"));

    assertThat(created.received).containsExactly(new Created("a"));
    assertThat(deleted.received).containsExactly(new Deleted("b"));
  }

  @Test
  void givenNoSubscribers_whenEventPublished_thenDoesNotThrow() {
    InProcessEventBus bus = new InProcessEventBus();
    assertThatCode(() -> bus.publish(new Created("order"))).doesNotThrowAnyException();
  }

  @Test
  void givenNullArguments_whenSubscribing_thenThrows() {
    InProcessEventBus bus = new InProcessEventBus();
    assertThatNullPointerException().isThrownBy(() -> bus.<Created>subscribe(null, event -> {}));
    assertThatNullPointerException().isThrownBy(() -> bus.subscribe(Created.class, null));
  }

  @Test
  void givenNullEvent_whenPublishing_thenThrows() {
    InProcessEventBus bus = new InProcessEventBus();
    assertThatNullPointerException().isThrownBy(() -> bus.publish(null));
  }
}
