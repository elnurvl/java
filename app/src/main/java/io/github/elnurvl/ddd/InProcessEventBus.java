package io.github.elnurvl.ddd;

import io.github.elnurvl.ddd.platform.event.DomainEvent;
import io.github.elnurvl.ddd.platform.event.EventHandler;
import io.github.elnurvl.ddd.platform.event.EventPublisher;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Synchronous, in-process {@link EventPublisher} — the monolith's default dispatcher.
 *
 * <p>The composition root {@linkplain #subscribe subscribes} handlers at startup; thereafter {@link
 * #publish} invokes every handler registered for the event's concrete type, in subscription order,
 * on the calling thread. Running inline keeps publishing inside the caller's transaction — simple
 * and consistent. Swap in an outbox-backed publisher when a subscriber's failure must not roll back
 * the publisher.
 */
public final class InProcessEventBus implements EventPublisher {

  private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>>
      handlersByType = new ConcurrentHashMap<>();

  /**
   * Registers a handler to be invoked for events of {@code eventType}.
   *
   * @param eventType the concrete event type to subscribe to; never {@code null}
   * @param handler the handler to invoke for such events; never {@code null}
   * @param <E> the event type
   * @throws NullPointerException if {@code eventType} or {@code handler} is {@code null}
   */
  public <E extends DomainEvent> void subscribe(Class<E> eventType, EventHandler<E> handler) {
    Objects.requireNonNull(eventType, "Event type cannot be null");
    Objects.requireNonNull(handler, "Handler cannot be null");
    handlersByType.computeIfAbsent(eventType, key -> new CopyOnWriteArrayList<>()).add(handler);
  }

  @Override
  public void publish(DomainEvent event) {
    Objects.requireNonNull(event, "Event cannot be null");
    for (EventHandler<? extends DomainEvent> handler :
        handlersByType.getOrDefault(event.getClass(), List.of())) {
      dispatch(handler, event);
    }
  }

  @SuppressWarnings("unchecked")
  private static void dispatch(EventHandler<? extends DomainEvent> handler, DomainEvent event) {
    ((EventHandler<DomainEvent>) handler).handle(event);
  }
}
