package io.github.elnurvl.ddd.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Enforces the modular-monolith boundaries: the shared kernel is a dependency sink and bounded
 * contexts stay decoupled from one another.
 *
 * <p>Lives in {@code app} — the composition root that depends on every module — so the import sees
 * the production classes of the whole assembly. Each top-level package under the base package is
 * one module: {@code shared} is the shared kernel and every other segment is a bounded context.
 */
@AnalyzeClasses(
    packages = "io.github.elnurvl.ddd",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

  /**
   * The shared kernel may be reused by any context but must depend on none of them, so it never
   * drags a context's concepts across boundaries. A dependency on the base package that is not
   * itself in the shared kernel can only be a bounded context (or the composition root).
   */
  @ArchTest
  static final ArchRule SHARED_KERNEL_DEPENDS_ON_NO_CONTEXT =
      noClasses()
          .that()
          .resideInAPackage("..shared..")
          .should()
          .dependOnClassesThat(
              resideInAPackage("io.github.elnurvl.ddd..").and(resideOutsideOfPackage("..shared..")))
          .as("the shared kernel must not depend on any bounded context")
          .because("it is a dependency sink that bounded contexts build upon");

  /**
   * Bounded contexts integrate through the shared kernel or the composition root, never by cyclic
   * reference to one another, keeping each module independently understandable and deployable.
   */
  @ArchTest
  static final ArchRule BOUNDED_CONTEXTS_ARE_FREE_OF_CYCLES =
      SlicesRuleDefinition.slices()
          .matching("io.github.elnurvl.ddd.(*)..")
          .should()
          .beFreeOfCycles();

  /**
   * A bounded context exposes a published contract under {@code ..api..} and keeps everything else
   * internal. Contexts may integrate only through each other's published API (the Open Host Service
   * / Anti-Corruption boundary), or through the shared kernel — never by reaching into another
   * context's internals.
   */
  @ArchTest
  static final ArchRule CONTEXTS_INTEGRATE_ONLY_VIA_PUBLISHED_API =
      SlicesRuleDefinition.slices()
          .matching("io.github.elnurvl.ddd.(*)..")
          .namingSlices("$1")
          .should()
          .notDependOnEachOther()
          .ignoreDependency(
              alwaysTrue(), resideInAPackage("..api..").or(resideInAPackage("..shared..")));

  /**
   * Within a context the dependencies point inward: {@code domain} depends on nothing, {@code
   * application} orchestrates it, and {@code infrastructure} and {@code api} are the outermost
   * adapters that no one depends on. Layers are matched across all contexts, so this guards the
   * dependency <em>direction</em>; cross-context leakage is covered by {@link
   * #CONTEXTS_INTEGRATE_ONLY_VIA_PUBLISHED_API}.
   */
  @ArchTest
  static final ArchRule CONTEXT_LAYERS_RESPECT_DEPENDENCY_DIRECTION =
      layeredArchitecture()
          .consideringOnlyDependenciesInAnyPackage("io.github.elnurvl.ddd..")
          .withOptionalLayers(true)
          .layer("Domain")
          .definedBy("..(*).domain..")
          .layer("Application")
          .definedBy("..(*).application..")
          .layer("Infrastructure")
          .definedBy("..(*).infrastructure..")
          .layer("Api")
          .definedBy("..(*).api..")
          .whereLayer("Infrastructure")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Api")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Application")
          .mayOnlyBeAccessedByLayers("Api", "Infrastructure")
          .whereLayer("Domain")
          .mayOnlyBeAccessedByLayers("Application", "Api", "Infrastructure");
}
