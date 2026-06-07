package io.github.elnurvl.ddd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/** Tests for {@link Main}. */
public class MainTest {
  @Test
  void givenTheAssembly_whenDescribed_thenIdentifiesTheMonolith() {
    assertThat(new Main().describe()).contains("modular monolith");
  }

  @Test
  void givenNoArguments_whenBooted_thenRunsWithoutError() {
    assertThatCode(() -> Main.main(new String[0])).doesNotThrowAnyException();
  }
}
