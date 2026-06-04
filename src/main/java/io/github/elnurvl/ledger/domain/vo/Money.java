package io.github.elnurvl.ledger.domain.vo;

import java.math.BigDecimal;

/** Non-negative monetary value backed by {@link BigDecimal}. */
public record Money(BigDecimal value) {
  /** Validates that the wrapped value is non-negative. */
  public Money {
    if (value.signum() < 0) {
      throw new IllegalArgumentException("Money cannot be negative");
    }
  }

  /** Constructs a {@link Money} from a decimal string. */
  public Money(String value) {
    this(new BigDecimal(value));
  }

  /** Constructs a {@link Money} from an integer. */
  public Money(int value) {
    this(BigDecimal.valueOf(value));
  }

  /** Constructs a {@link Money} from a double. */
  public Money(double value) {
    this(BigDecimal.valueOf(value));
  }

  /** Returns a new {@link Money} representing this plus {@code other}. */
  public Money add(Money other) {
    return new Money(value.add(other.value));
  }

  /** Returns a new {@link Money} representing this minus {@code other}. */
  public Money subtract(Money other) {
    return new Money(value.subtract(other.value));
  }
}
