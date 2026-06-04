package io.github.elnurvl.ledger.domain.vo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Non-negative monetary value backed by {@link BigDecimal} and tagged with a {@link Currency}.
 *
 * <p>The amount is normalized to the currency's minor-unit scale (e.g. 2 fraction digits for USD, 0
 * for JPY) at construction. This makes equality and ordering scale-insensitive yet consistent, and
 * gives {@link #allocate(int)} a well-defined unit to split into.
 */
public record Money(BigDecimal value, Currency currency) implements Comparable<Money> {
  /** Currency assumed when a constructor is called without an explicit one. */
  public static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

  /** The zero amount in the {@link #DEFAULT_CURRENCY}. */
  public static final Money ZERO = new Money(BigDecimal.ZERO);

  /**
   * Validates and normalizes the amount to the currency's minor-unit scale.
   *
   * @throws NullPointerException if {@code value} or {@code currency} is {@code null}
   * @throws ArithmeticException if {@code value} has more precision than the currency allows
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money {
    Objects.requireNonNull(value, "Money value cannot be null");
    Objects.requireNonNull(currency, "Currency cannot be null");
    value = value.setScale(scaleOf(currency), RoundingMode.UNNECESSARY);
    if (value.signum() < 0) {
      throw new NegativeMoneyException();
    }
  }

  /**
   * Constructs a {@link Money} in the {@link #DEFAULT_CURRENCY}.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public Money(BigDecimal value) {
    this(value, DEFAULT_CURRENCY);
  }

  /**
   * Creates a {@link Money} from a decimal string in the given currency.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws NumberFormatException if {@code value} is not a valid decimal representation
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public static Money of(String value, Currency currency) {
    return new Money(
        new BigDecimal(Objects.requireNonNull(value, "Money value cannot be null")), currency);
  }

  /**
   * Creates a {@link Money} from a decimal string in the {@link #DEFAULT_CURRENCY}.
   *
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws NumberFormatException if {@code value} is not a valid decimal representation
   * @throws NegativeMoneyException if {@code value} is negative
   */
  public static Money of(String value) {
    return of(value, DEFAULT_CURRENCY);
  }

  /**
   * Returns a new {@link Money} representing this plus {@code other}.
   *
   * @throws IllegalArgumentException if {@code other} is in a different currency
   */
  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(value.add(other.value), currency);
  }

  /**
   * Returns a new {@link Money} representing this minus {@code other}.
   *
   * @throws IllegalArgumentException if {@code other} is in a different currency
   * @throws NegativeMoneyException if the result would be negative
   */
  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(value.subtract(other.value), currency);
  }

  /**
   * Returns a new {@link Money} scaled by {@code factor} (e.g. a tax or interest rate).
   *
   * @throws NullPointerException if {@code factor} is {@code null}
   * @throws ArithmeticException if the product has more precision than the currency allows
   * @throws NegativeMoneyException if {@code factor} is negative
   */
  public Money multiplyBy(BigDecimal factor) {
    return new Money(
        value.multiply(Objects.requireNonNull(factor, "Factor cannot be null")), currency);
  }

  /**
   * Defines a total order over all amounts, by currency code first and then amount. This is a
   * deterministic ordering for sorting and sorted collections, <em>not</em> an economic comparison:
   * cross-currency ordering is lexical (e.g. {@code AZN} sorts before {@code USD}) and does not
   * imply relative worth. Consistent with {@code equals}: returns {@code 0} if and only if the two
   * are equal.
   */
  @Override
  public int compareTo(Money other) {
    int byCurrency = currency.getCurrencyCode().compareTo(other.currency.getCurrencyCode());
    return byCurrency != 0 ? byCurrency : value.compareTo(other.value);
  }

  /**
   * Splits this amount into {@code parts} shares whose sum is exactly this amount — no value is
   * created or lost. Any indivisible remainder is distributed one minor unit at a time to the
   * earlier shares, so the first shares may be one unit larger than the last. Each share carries
   * this amount's currency.
   *
   * @throws IllegalArgumentException if {@code parts} is not positive
   */
  public List<Money> allocate(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be positive");
    }
    int scale = value.scale();
    BigInteger total = value.scaleByPowerOfTen(scale).toBigIntegerExact();
    BigInteger count = BigInteger.valueOf(parts);
    BigInteger base = total.divide(count);
    int remainder = total.subtract(base.multiply(count)).intValueExact();
    List<Money> shares = new ArrayList<>(parts);
    for (int i = 0; i < parts; i++) {
      BigInteger units = i < remainder ? base.add(BigInteger.ONE) : base;
      shares.add(new Money(new BigDecimal(units).scaleByPowerOfTen(-scale), currency));
    }
    return List.copyOf(shares);
  }

  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " vs " + other.currency);
    }
  }

  private static int scaleOf(Currency currency) {
    return Math.max(currency.getDefaultFractionDigits(), 0);
  }
}
