package io.github.elnurvl.ledger.domain.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Tests for {@link Money}. */
public class MoneyTest {
  @Test
  void backedByBigDecimal() {
    Money money = new Money("3.5");

    assertEquals(0, money.value().compareTo(new BigDecimal("3.5")));
  }

  @Test
  void addReturnsResultMoney() {
    Money money = new Money(2);

    Money summed = money.add(new Money(3));

    assertEquals(0, summed.value().compareTo(new BigDecimal("5")));
  }

  @Test
  void subtractReturnsResultMoney() {
    Money money = new Money(3);

    Money diff = money.subtract(new Money(2));

    assertEquals(0, diff.value().compareTo(new BigDecimal("1")));
  }

  @Test
  void subtractThrowsErrorIfResultIsNegative() {
    Money money = new Money(2);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> money.subtract(new Money(3)));
    assertEquals("Money cannot be negative", ex.getMessage());
  }

  @Test
  void throwsIfMoneyIsNegative() {
    Exception ex = assertThrows(NegativeMoneyException.class, () -> new Money(-1));
    assertEquals("Money cannot be negative", ex.getMessage());
  }

  @Test
  void constructWorksWithDouble() {
    // Act
    Money money = new Money(0.1);

    // Assert
    assertEquals(0, money.value().compareTo(new BigDecimal("0.1")));
  }

  @Test
  void equals_sameValue() {
    // Arrange
    Money money1 = new Money(1);
    Money money2 = new Money("1");

    // Act
    boolean result = money1.equals(money2);

    // Assert
    assertTrue(result);
  }

  @Test
  void equals_ignoresScale() {
    // Arrange
    Money money1 = new Money("3.5");
    Money money2 = new Money("3.50");

    // Assert
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
    Exception ex = assertThrows(NullPointerException.class, () -> new Money((String) null));
    assertEquals("Money value cannot be null", ex.getMessage());
  }

  @Test
  void subtract_returnsZero_whenEqual() {
    Money money = new Money(2);

    Money diff = money.subtract(new Money(2));

    assertEquals(0, diff.value().compareTo(BigDecimal.ZERO));
  }

  @Test
  void zero_isZeroAmount() {
    assertEquals(0, Money.ZERO.value().compareTo(BigDecimal.ZERO));
  }

  @Test
  void multiplyBy_scalesValue() {
    Money money = new Money("2.5");

    Money scaled = money.multiplyBy(new BigDecimal("4"));

    assertEquals(new Money(10), scaled);
  }

  @Test
  void multiplyBy_throwsWhenFactorIsNull() {
    Money money = new Money(1);

    Exception ex = assertThrows(NullPointerException.class, () -> money.multiplyBy(null));
    assertEquals("Factor cannot be null", ex.getMessage());
  }

  @Test
  void multiplyBy_throwsWhenFactorIsNegative() {
    Money money = new Money(1);

    assertThrows(NegativeMoneyException.class, () -> money.multiplyBy(new BigDecimal("-1")));
  }

  @Test
  void compareTo_ordersByValue() {
    assertTrue(new Money(2).compareTo(new Money(3)) < 0);
    assertTrue(new Money(5).compareTo(new Money(3)) > 0);
  }

  @Test
  void compareTo_isZeroForEqualValuesIgnoringScale() {
    assertEquals(0, new Money("3.5").compareTo(new Money("3.50")));
  }
}
