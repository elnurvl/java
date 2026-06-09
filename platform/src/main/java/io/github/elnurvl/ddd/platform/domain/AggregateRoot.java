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
 * <p>An aggregate root is the consistency boundary, so it is also the unit of optimistic locking.
 * Each root carries a {@linkplain #version() version} that counts its committed state transitions:
 * a brand-new aggregate starts at {@code 0}, one {@linkplain #AggregateRoot(Object, long)
 * reconstituted} from storage carries the persisted value, and infrastructure calls {@link
 * #nextVersion()} after a successful write. The version is deliberately <em>not</em> part of {@link
 * Entity#equals(Object) equals}/{@code hashCode} — identity is by id alone, however much the state
 * (and its version) has since moved on.
 *
 * @param <IdT> the type of this aggregate's identifier
 */
public abstract class AggregateRoot<IdT> extends Entity<IdT> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  private long version;

  /**
   * Creates a brand-new aggregate root at version {@code 0}.
   *
   * @param id the identifier; never {@code null}
   * @throws NullPointerException if {@code id} is {@code null}
   */
  protected AggregateRoot(IdT id) {
    this(id, 0L);
  }

  /**
   * Creates an aggregate root with the given identity and persisted version. Intended for
   * reconstitution from storage; behaviour-driven construction should use {@link
   * #AggregateRoot(Object)} and let the version start at {@code 0}.
   *
   * @param id the identifier; never {@code null}
   * @param version the persisted version; never negative
   * @throws NullPointerException if {@code id} is {@code null}
   * @throws IllegalArgumentException if {@code version} is negative
   */
  protected AggregateRoot(IdT id, long version) {
    super(id);
    if (version < 0) {
      throw new IllegalArgumentException("Version cannot be negative: " + version);
    }
    this.version = version;
  }

  /**
   * Returns this aggregate's optimistic-locking version — the number of committed state transitions
   * since it was first created.
   *
   * @return the current version; never negative
   */
  public final long version() {
    return version;
  }

  /**
   * Advances the version by one to reflect a committed write.
   *
   * <p>Called by the persistence layer after a successful save (the compare-and-set has already
   * matched the prior version in storage), so the in-memory instance stays usable for a subsequent
   * save within the same unit of work. This is an increment, not an arbitrary setter: callers
   * cannot rewind or forge a version, preserving the monotonic invariant.
   */
  public final void nextVersion() {
    version++;
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
