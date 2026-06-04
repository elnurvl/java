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
    Exception ex = assertThrows(IllegalArgumentException.class, () -> new Money(-1));
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
}
