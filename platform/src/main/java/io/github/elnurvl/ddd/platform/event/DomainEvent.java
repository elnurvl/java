package io.github.elnurvl.ddd.platform.event;

/**
 * Marker for something that happened in the domain and may be published for others to react to.
 *
 * <p>This is a purely technical contract — it carries no business meaning of its own. A context's
 * <em>integration</em> events (the ones it publishes under its {@code ..api..} package) implement
 * it so they can travel through the {@link EventPublisher}.
 */
public interface DomainEvent {}
