package io.github.elnurvl.ddd.platform.event;

/**
 * Marker for an event a bounded context publishes as part of its publicly supported contract.
 *
 * <p>Unlike a domain event, an integration event is meant to cross context boundaries: a context
 * declares its integration events under its {@code ..api..} package (its published language) and
 * emits them through an {@link EventPublisher}. Other contexts subscribe to these — never to
 * another context's internal domain events. Being a contract, integration events should be kept
 * stable and versioned deliberately.
 */
public interface IntegrationEvent {}
