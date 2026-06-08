package io.github.elnurvl.ddd.platform.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

/** Tests for {@link Entity}. */
class EntityTest {

  private static final class Product extends Entity<String> {
    Product(String id) {
      super(id);
    }
  }

  private static final class Customer extends Entity<String> {
    Customer(String id) {
      super(id);
    }
  }

  @Test
  void givenNullId_whenConstructed_thenThrows() {
    assertThatNullPointerException().isThrownBy(() -> new Product(null));
  }

  @Test
  void givenAnId_whenQueried_thenReturnsIt() {
    assertThat(new Product("p1").id()).isEqualTo("p1");
  }

  @Test
  void givenSameTypeAndId_whenCompared_thenEqualWithSameHashCode() {
    Product a = new Product("p1");
    Product b = new Product("p1");
    assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
  }

  @Test
  void givenSameTypeDifferentId_whenCompared_thenNotEqual() {
    assertThat(new Product("p1")).isNotEqualTo(new Product("p2"));
  }

  @Test
  void givenSameIdDifferentType_whenCompared_thenNotEqual() {
    assertThat(new Product("x")).isNotEqualTo(new Customer("x"));
  }

  @Test
  void givenItself_whenCompared_thenEqual() {
    Product product = new Product("p1");
    assertThat(product.equals(product)).isTrue();
  }

  @Test
  void givenNullOrUnrelatedType_whenCompared_thenNotEqual() {
    Product product = new Product("p1");
    assertThat(product.equals(null)).isFalse();
    assertThat(product.equals("p1")).isFalse();
  }

  @Test
  void whenStringified_thenIncludesTypeAndId() {
    assertThat(new Product("p1").toString()).contains("Product").contains("p1");
  }
}
