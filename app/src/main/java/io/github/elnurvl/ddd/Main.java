package io.github.elnurvl.ddd;

/**
 * Composition root and entry point of the modular monolith.
 *
 * <p>The deployable assembly lives here and wires together the bounded-context modules. Each
 * bounded context is a separate Gradle module that depends only on {@code common} (where shared
 * building blocks such as {@code Money} live), never on another context directly.
 */
public final class Main {

  /**
   * Boots the monolith.
   *
   * @param args command-line arguments (ignored)
   */
  public static void main(String[] args) {
    System.out.println(new Main().describe());
  }

  /**
   * Describes the running assembly.
   *
   * @return a human-readable identifier for the monolith
   */
  String describe() {
    return "ddd modular monolith";
  }
}
