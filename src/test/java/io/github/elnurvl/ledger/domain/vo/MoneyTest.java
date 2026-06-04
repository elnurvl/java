package io.github.elnurvl.ledger.domain.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for {@link Money}. */
public class MoneyTest {
  private static final Currency USD = Currency.getInstance("USD");
  private static final Currency AZN = Currency.getInstance("AZN");
  private static final Currency JPY = Currency.getInstance("JPY");

  @Test
  void backedByBigDecimal() {
    Money money = Money.of("3.5");

    assertEquals(0, money.value().compareTo(new BigDecimal("3.5")));
  }

  @Test
  void addReturnsResultMoney() {
    Money money = Money.of("2");

    Money summed = money.add(Money.of("3"));

    assertEquals(0, summed.value().compareTo(new BigDecimal("5")));
  }

  @Test
  void subtractReturnsResultMoney() {
    Money money = Money.of("3");

    Money diff = money.subtract(Money.of("2"));

    assertEquals(0, diff.value().compareTo(new BigDecimal("1")));
  }

  @Test
  void subtractThrowsErrorIfResultIsNegative() {
    Money money = Money.of("2");

    Exception ex =
        assertThrows(IllegalArgumentException.class, () -> money.subtract(Money.of("3")));
    assertEquals("Money cannot be negative", ex.getMessage());
  }

  @Test
  void throwsIfMoneyIsNegative() {
    Exception ex = assertThrows(NegativeMoneyException.class, () -> Money.of("-1"));
    assertEquals("Money cannot be negative", ex.getMessage());
  }

  @Test
  void constructsFromFractionalString() {
    Money money = Money.of("0.1");

    assertEquals(0, money.value().compareTo(new BigDecimal("0.1")));
  }

  @Test
  void equals_sameValue() {
    Money money1 = Money.of("1");
    Money money2 = Money.of("1");

    assertEquals(money1, money2);
  }

  @Test
  void equals_ignoresScale() {
    Money money1 = Money.of("3.5");
    Money money2 = Money.of("3.50");

    assertEquals(money1, money2);
    assertEquals(money1.hashCode(), money2.hashCode());
  }

  @Test
  void throws_whenBigDecimalIsNull() {
    Exception ex = assertThrows(NullPointerException.class, () -> new Money((BigDecimal) null));
    assertEquals("Money value cannot be null", ex.getMessage());
  }

  @Test
  void throws_whenStringIsNull() {
    Exception ex = assertThrows(NullPointerException.class, () -> Money.of(null));
    assertEquals("Money value cannot be null", ex.getMessage());
  }

  @Test
  void subtract_returnsZero_whenEqual() {
    Money money = Money.of("2");

    Money diff = money.subtract(Money.of("2"));

    assertEquals(0, diff.value().compareTo(BigDecimal.ZERO));
  }

  @Test
  void zero_isZeroAmount() {
    assertEquals(0, Money.ZERO.value().compareTo(BigDecimal.ZERO));
  }

  @Test
  void multiplyBy_scalesValue() {
    Money money = Money.of("2.5");

    Money scaled = money.multiplyBy(new BigDecimal("4"));

    assertEquals(Money.of("10"), scaled);
  }

  @Test
  void multiplyBy_throwsWhenFactorIsNull() {
    Money money = Money.of("1");

    Exception ex = assertThrows(NullPointerException.class, () -> money.multiplyBy(null));
    assertEquals("Factor cannot be null", ex.getMessage());
  }

  @Test
  void multiplyBy_throwsWhenFactorIsNegative() {
    Money money = Money.of("1");

    assertThrows(NegativeMoneyException.class, () -> money.multiplyBy(new BigDecimal("-1")));
  }

  @Test
  void compareTo_ordersByValue() {
    assertTrue(Money.of("2").compareTo(Money.of("3")) < 0);
    assertTrue(Money.of("5").compareTo(Money.of("3")) > 0);
  }

  @Test
  void compareTo_isZeroForEqualValuesIgnoringScale() {
    assertEquals(0, Money.of("3.5").compareTo(Money.of("3.50")));
  }

  @Test
  void allocate_splitsEvenly() {
    List<Money> shares = Money.of("9").allocate(3);

    assertEquals(List.of(Money.of("3"), Money.of("3"), Money.of("3")), shares);
  }

  @Test
  void allocate_distributesRemainderToEarlierShares() {
    List<Money> shares = Money.of("10").allocate(3);

    assertEquals(List.of(Money.of("3.34"), Money.of("3.33"), Money.of("3.33")), shares);
  }

  @Test
  void allocate_conservesTotal() {
    Money original = Money.of("10.01");

    Money sum = original.allocate(3).stream().reduce(Money.ZERO, Money::add);

    assertEquals(original, sum);
  }

  @Test
  void allocate_throwsWhenPartsNotPositive() {
    Money money = Money.of("10");

    Exception ex = assertThrows(IllegalArgumentException.class, () -> money.allocate(0));
    assertEquals("Parts must be positive", ex.getMessage());
  }

  @Test
  void constructs_withDefaultCurrencyUsd() {
    Money money = Money.of("4");

    assertEquals(USD, money.currency());
  }

  @Test
  void normalizesToCurrencyScale() {
    Money usd = Money.of("3.5", USD);
    Money jpy = Money.of("3", JPY);

    assertEquals(2, usd.value().scale());
    assertEquals(0, jpy.value().scale());
  }

  @Test
  void creates_inGivenCurrency() {
    Money money = Money.of("2.5", AZN);

    assertEquals(AZN, money.currency());
    assertEquals(0, money.value().compareTo(new BigDecimal("2.5")));
  }

  @Test
  void allocate_keepsCurrency() {
    List<Money> shares = Money.of("10", AZN).allocate(3);

    assertEquals(AZN, shares.get(0).currency());
  }

  @Test
  void compareTo_ordersByCurrencyThenAmount() {
    Money usd = Money.of("3", USD);
    Money azn = Money.of("3", AZN);

    // AZN sorts before USD lexically, regardless of amount.
    assertTrue(azn.compareTo(usd) < 0);
    assertTrue(usd.compareTo(azn) > 0);
    assertTrue(Money.of("1", USD).compareTo(Money.of("1000", AZN)) > 0);
  }

  @Test
  void add_throws_ifCurrenciesMismatch() {
    Money money = Money.of("3", USD);
    Money money2 = Money.of("3", AZN);

    assertThrows(IllegalArgumentException.class, () -> money.add(money2));
  }

  @Test
  void notEquals_acrossCurrencies() {
    Money usd = Money.of("1", USD);
    Money azn = Money.of("1", AZN);

    assertNotEquals(usd, azn);
  }

  @Test
  void toString_includesAmountAndCurrency() {
    String text = Money.of("3.5", USD).toString();

    assertTrue(text.contains("3.50"));
    assertTrue(text.contains("USD"));
  }
}
