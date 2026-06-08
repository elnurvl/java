package io.github.elnurvl.ddd.platform.event;

/**
 * Reacts to a published {@link IntegrationEvent} of type {@code E}.
 *
 * <p>A subscribing context implements this in its application layer against another context's
 * published integration-event type. The composition root registers the handler with the dispatcher.
 *
 * @param <E> the integration event type this handler consumes
 */
public interface EventHandler<E extends IntegrationEvent> {

  /**
   * Handles a published integration event.
   *
   * @param event the event to react to; never {@code null}
   */
  void handle(E event);
}
