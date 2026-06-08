package io.github.elnurvl.ddd.platform.domain;

import java.util.Objects;

/**
 * Base class for domain entities: objects defined by a continuous identity rather than by their
 * attributes.
 *
 * <p>Two entities are equal when they are of the same concrete type and share the same {@code id},
 * however much their other state has since diverged. The identity is fixed at construction, so
 * {@link #equals} and {@link #hashCode} are stable for the object's whole life and are declared
 * {@code final} to keep subclasses from weakening that contract.
 *
 * @param <IdT> the type of this entity's identifier
 */
public abstract class Entity<IdT> {

  private final IdT id;

  /**
   * Creates an entity with the given identity.
   *
   * @param id the identifier; never {@code null}
   * @throws NullPointerException if {@code id} is {@code null}
   */
  protected Entity(IdT id) {
    this.id = Objects.requireNonNull(id, "Entity id cannot be null");
  }

  /**
   * Returns this entity's identifier.
   *
   * @return the identifier; never {@code null}
   */
  public final IdT id() {
    return id;
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    return id.equals(((Entity<?>) other).id);
  }

  @Override
  public final int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[id=" + id + "]";
  }
}
