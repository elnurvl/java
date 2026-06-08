package io.github.elnurvl.ddd.platform.event;

/**
 * Outbound port for emitting domain events without knowing who, if anyone, handles them.
 *
 * <p>A context's application layer depends on this abstraction; the composition root supplies the
 * concrete dispatcher (e.g. an in-process bus, or later an outbox-backed implementation), so the
 * publish mechanism can change without touching any domain or application code.
 */
public interface EventPublisher {

  /**
   * Publishes {@code event} to every interested handler.
   *
   * @param event the domain event to dispatch; never {@code null}
   */
  void publish(DomainEvent event);
}
