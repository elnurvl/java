package io.github.elnurvl.ddd.platform.event;

/**
 * Outbound port for publishing a context's integration events without knowing who consumes them.
 *
 * <p>A context's application layer depends on this abstraction to announce something other contexts
 * may care about; the composition root supplies the concrete dispatcher (e.g. an in-process bus, or
 * later an outbox-backed implementation), so the transport can change without touching domain or
 * application code.
 */
public interface EventPublisher {

  /**
   * Publishes {@code event} to every interested subscriber.
   *
   * @param event the integration event to dispatch; never {@code null}
   */
  void publish(IntegrationEvent event);
}
