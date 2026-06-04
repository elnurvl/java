package io.github.elnurvl.ledger.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

/** Non-negative monetary value backed by {@link BigDecimal}. */
public record Money(BigDecimal value) implements Comparable<Money> {
  /** The zero amount. */
  public static final Money ZERO = new Money(0);

  /**
   * Validates and normalizes the wrapped value.
   *
   * <p>Trailing zeros are stripped so that values which are numerically equal compare equal under
   * the record's generated {@code equals}/{@code hashCode} (e.g. {@code "3.5"} equals {@code
   * "3.50"}).
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money {
    Objects.requireNonNull(value, "Money value cannot be null");
    if (value.signum() < 0) {
      throw new NegativeMoneyException();
    }
    value = value.stripTrailingZeros();
  }

  /**
   * Constructs a {@link Money} from a decimal string.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws NumberFormatException if {@code value} is not a valid decimal representation
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money(String value) {
    this(new BigDecimal(Objects.requireNonNull(value, "Money value cannot be null")));
  }

  /**
   * Constructs a {@link Money} from an integer.
   *
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money(int value) {
    this(BigDecimal.valueOf(value));
  }

  /**
   * Constructs a {@link Money} from a double.
   *
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money(double value) {
    this(BigDecimal.valueOf(value));
  }

  /** Returns a new {@link Money} representing this plus {@code other}. */
  public Money add(Money other) {
    return new Money(value.add(other.value));
  }

  /**
   * Returns a new {@link Money} representing this minus {@code other}.
   *
   * @throws NegativeMoneyException if the result would be negative
   */
  public Money subtract(Money other) {
    return new Money(value.subtract(other.value));
  }

  /**
   * Returns a new {@link Money} scaled by {@code factor} (e.g. a tax or interest rate).
   *
   * @throws NullPointerException if {@code factor} is {@code null}
   * @throws NegativeMoneyException if {@code factor} is negative
   */
  public Money multiplyBy(BigDecimal factor) {
    return new Money(value.multiply(Objects.requireNonNull(factor, "Factor cannot be null")));
  }

  /**
   * Orders by numeric value. Consistent with {@code equals}: {@code compareTo} returns {@code 0} if
   * and only if the two amounts are equal.
   */
  @Override
  public int compareTo(Money other) {
    return value.compareTo(other.value);
  }
}
