package io.github.elnurvl.ddd.common;

/** Thrown when a {@link Money} value would be negative, which the domain forbids. */
public class NegativeMoneyException extends IllegalArgumentException {
  /** Creates the exception with the canonical domain message. */
  public NegativeMoneyException() {
    super("Money cannot be negative");
  }
}
