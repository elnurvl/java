package io.github.elnurvl.ddd.platform.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for aggregate roots: the single entry point to an aggregate and the only place that
 * records domain events.
 *
 * <p>Behaviour that mutates the aggregate registers an event with {@link #registerEvent}. The
 * application layer later {@linkplain #pullDomainEvents() pulls} those events — typically right
 * after persisting the aggregate — and hands them to an {@code EventPublisher}. Pulling is
 * destructive, so each recorded event is dispatched exactly once.
 *
 * @param <IdT> the type of this aggregate's identifier
 */
public abstract class AggregateRoot<IdT> extends Entity<IdT> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /**
   * Creates an aggregate root with the given identity.
   *
   * @param id the identifier; never {@code null}
   * @throws NullPointerException if {@code id} is {@code null}
   */
  protected AggregateRoot(IdT id) {
    super(id);
  }

  /**
   * Records a domain event to be published once this aggregate's changes are committed.
   *
   * @param event the event to record; never {@code null}
   * @throws NullPointerException if {@code event} is {@code null}
   */
  protected final void registerEvent(DomainEvent event) {
    domainEvents.add(Objects.requireNonNull(event, "Domain event cannot be null"));
  }

  /**
   * Returns the events recorded since the last pull and clears the buffer, so each event is
   * published exactly once.
   *
   * @return an immutable copy of the recorded events in registration order; never {@code null}
   */
  public final List<DomainEvent> pullDomainEvents() {
    List<DomainEvent> recorded = List.copyOf(domainEvents);
    domainEvents.clear();
    return recorded;
  }
}
