package io.github.elnurvl.ddd.platform.domain;

/**
 * Marker for something significant that happened inside a bounded context.
 *
 * <p>Domain events are raised by {@link AggregateRoot aggregates} and handled <em>within the same
 * context</em> — to keep aggregates consistent or to drive local side effects. They speak the
 * context's own language and stay internal; they are never placed on the cross-context bus. To
 * notify other contexts, the application layer translates a domain event into a published {@code
 * IntegrationEvent}.
 */
public interface DomainEvent {}
