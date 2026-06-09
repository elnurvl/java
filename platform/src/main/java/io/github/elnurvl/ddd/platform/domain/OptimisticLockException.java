package io.github.elnurvl.ddd.platform.domain;

/**
 * Signals that a write was rejected because the aggregate changed in storage since it was loaded —
 * the optimistic-locking compare-and-set found a newer version than the one held in memory.
 *
 * <p>A context's repository implementation throws it when a save's {@code WHERE id = ? AND version
 * = ?} update matches no row. Shared as a vocabulary type — not an imposed repository shape — so
 * every context signals the same conflict in the same language while keeping its own repository
 * interface. It is unchecked: a concurrency conflict is an exceptional, retry-or-reload outcome
 * rather than a path every caller must declare, matching how persistence frameworks conventionally
 * surface the same condition.
 */
public final class OptimisticLockException extends RuntimeException {

  /**
   * Creates an exception with the given detail message.
   *
   * @param message the detail message
   */
  public OptimisticLockException(String message) {
    super(message);
  }

  /**
   * Creates an exception describing a stale write against a specific aggregate.
   *
   * @param id the aggregate's identifier
   * @param expectedVersion the version held in memory that no longer matches storage
   * @return an exception naming the aggregate and the stale version
   */
  public static OptimisticLockException forAggregate(Object id, long expectedVersion) {
    return new OptimisticLockException(
        "Aggregate " + id + " was modified concurrently; expected version " + expectedVersion);
  }
}
