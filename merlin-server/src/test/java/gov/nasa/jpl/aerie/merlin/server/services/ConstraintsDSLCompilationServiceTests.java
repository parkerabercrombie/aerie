package gov.nasa.jpl.aerie.merlin.server.services;

import gov.nasa.jpl.aerie.constraints.tree.*;
import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue;
import gov.nasa.jpl.aerie.merlin.server.models.PlanId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConstraintsDSLCompilationServiceTests {
  private static final PlanId PLAN_ID = new PlanId(1L);
  ConstraintsDSLCompilationService constraintsDSLCompilationService;

  @BeforeAll
  void setUp() throws IOException {
    constraintsDSLCompilationService = new ConstraintsDSLCompilationService(new TypescriptCodeGenerationService());
  }

  @AfterAll
  void tearDown() {
    constraintsDSLCompilationService.close();
  }

  private <T> void checkSuccessfulCompilation(String constraint, Expression<T> expected) {
    final ConstraintsDSLCompilationService.ConstraintsDSLCompilationResult result;
    result = constraintsDSLCompilationService.compileConstraintsDSL(PLAN_ID, constraint);
    if (result instanceof ConstraintsDSLCompilationService.ConstraintsDSLCompilationResult.Success r) {
      assertEquals(expected, r.constraintExpression());
    } else if (result instanceof ConstraintsDSLCompilationService.ConstraintsDSLCompilationResult.Error r) {
      fail(r.toString());
    }
  }

  private void checkFailedCompilation(String constraint, String error) {
    final ConstraintsDSLCompilationService.ConstraintsDSLCompilationResult.Error actualErrors;
    actualErrors = (ConstraintsDSLCompilationService.ConstraintsDSLCompilationResult.Error) constraintsDSLCompilationService.compileConstraintsDSL(
        PLAN_ID, constraint
    );
    if (actualErrors.errors()
                    .stream()
                    .noneMatch(e -> e.message().contains(error))) {
      fail("Expected error:\n" + error + "\nIn list of errors:\n" + actualErrors.errors() + "\n");
    }
  }

  @Test
  void testConstraintsDSL_helper_function()
  {
    checkSuccessfulCompilation(
      """
        export default function myConstraint() {
          return times2(Real.Resource("my_resource")).changed()
        }
        function times2(e: Real): Real {
          return e.times(2)
        }
      """,
      new ViolationsOf(
          new Changed<>(new ProfileExpression<>(new Times(new RealResource("my_resource"), 2.0)))
      )
    );
  }

  @Test
  void testConstraintsDSL_variable_not_defined() {
    checkFailedCompilation(
        """
          export default function myConstraint() {
            const x = 5;
            return times2(Real.Resource("my_resource")).changed()
          }
          function times2(e: Real): Real {
            return e.times(x)
          }
        """,
       "TypeError: TS2304 Cannot find name 'x'."
    );
  }

  @Test
  void testConstraintsDSL_wrong_return_type() {
    checkFailedCompilation(
        """
          export default function myConstraint() {
             return Real.Resource("my_resource");
          }
        """,
        "TypeError: TS2322 Incorrect return type. Expected: 'Constraint', Actual: 'Real'."
    );
  }

  //// TESTS FOR `Discrete` CLASS API

  @Test
  void testDiscreteResource() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Discrete.Resource("my_resource").changed();
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new DiscreteResource("my_resource"))))
    );
  }

  @Test
  void testDiscreteValue() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Discrete.Value(5).changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new DiscreteValue(SerializedValue.of(5)))))
    );
  }

  @Test
  void testDiscreteParameter() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Discrete.Parameter("my_activity", "my_parameter").changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new DiscreteParameter("my_activity", "my_parameter"))))
    );
  }

  @Test
  void testTransition() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Discrete.Resource("my_resource").transition("from one state", "to another");
            }
        """,
        new ViolationsOf(
            new Transition(
                new DiscreteResource("my_resource"),
                SerializedValue.of("from one state"),
                SerializedValue.of("to another")
            )
        )
    );
  }

  @Test
  void testTransitionMismatchedTypes() {
    checkFailedCompilation(
        """
          export default () => {
            return Discrete.Resource("my_resource").transition("from one state", 5);
          }
        """,
        "TS2345 Argument of type 'number' is not assignable to parameter of type 'string'."
    );
  }

  @Test
  void testDiscreteEqual() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Discrete.Resource("my_resource").equal(Discrete.Value("hello there"));
        }
        """,
        new ViolationsOf(new Equal<>(new DiscreteResource("my_resource"), new DiscreteValue(SerializedValue.of("hello there"))))
    );
  }

  @Test
  void testDiscreteNotEqual() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Discrete.Resource("my_resource").notEqual(Discrete.Value("hello there"));
        }
        """,
        new ViolationsOf(new NotEqual<>(new DiscreteResource("my_resource"), new DiscreteValue(SerializedValue.of("hello there"))))
    );
  }

  @Test
  void testDiscreteChanged() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Discrete.Value(4).changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new DiscreteValue(SerializedValue.of(4)))))
    );
  }

  @Test
  void testAutomaticWrapAnyInDiscreteValue() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Discrete.Resource("my_resource").equal("some value")
          }
        """,
        new ViolationsOf(new Equal<>(new DiscreteResource("my_resource"), new DiscreteValue(SerializedValue.of("some value"))))
    );
  }

  //// TESTS FOR `Real` CLASS API

  @Test
  void testRealResource() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Real.Resource("my_resource").changed();
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new RealResource("my_resource"))))
    );
  }

  @Test
  void testRealValue() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Real.Value(5).changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new RealValue(5.0))))
    );
  }

  @Test
  void testRealParameter() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Real.Parameter("my_activity", "my_parameter").changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new RealParameter("my_activity", "my_parameter"))))
    );
  }

  @Test
  void testRate() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").rate().equal(Real.Value(4.0))
            }
        """,
        new ViolationsOf(new Equal<>(new Rate(new RealResource("my_resource")), new RealValue(4.0)))
    );
  }

  @Test
  void testTimes() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").times(2).equal(Real.Value(4.0))
            }
        """,
        new ViolationsOf(new Equal<>(new Times(new RealResource("my_resource"), 2.0), new RealValue(4.0)))
    );
  }

  @Test
  void testPlus() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").plus(Real.Value(2.0)).equal(Real.Value(4.0))
            }
        """,
        new ViolationsOf(new Equal<>(new Plus(new RealResource("my_resource"), new RealValue(2.0)), new RealValue(4.0)))
    );
  }

  @Test
  void testLessThan() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").lessThan(Real.Value(2.0))
            }
        """,
        new ViolationsOf(new LessThan(new RealResource("my_resource"), new RealValue(2.0)))
    );
  }

  @Test
  void testLessThanOrEqual() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").lessThanOrEqual(Real.Value(2.0))
            }
        """,
        new ViolationsOf(new LessThanOrEqual(new RealResource("my_resource"), new RealValue(2.0)))
    );
  }

  @Test
  void testGreaterThan() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").greaterThan(Real.Value(2.0))
            }
        """,
        new ViolationsOf(new GreaterThan(new RealResource("my_resource"), new RealValue(2.0)))
    );
  }

  @Test
  void testGreaterThanOrEqual() {
    checkSuccessfulCompilation(
        """
            export default() => {
              return Real.Resource("my_resource").greaterThanOrEqual(Real.Value(2.0))
            }
        """,
        new ViolationsOf(new GreaterThanOrEqual(new RealResource("my_resource"), new RealValue(2.0)))
    );
  }

  @Test
  void testRealEqual() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Real.Resource("my_resource").equal(Real.Value(-1));
        }
        """,
        new ViolationsOf(new Equal<>(new RealResource("my_resource"), new RealValue(-1.0)))
    );
  }

  @Test
  void testRealNotEqual() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Real.Resource("my_resource").notEqual(Real.Value(-1));
        }
        """,
        new ViolationsOf(new NotEqual<>(new RealResource("my_resource"), new RealValue(-1.0)))
    );
  }

  @Test
  void testRealChanged() {
    checkSuccessfulCompilation(
        """
            export default () => {
              return Real.Value(4).changed()
            }
        """,
        new ViolationsOf(new Changed<>(new ProfileExpression<>(new RealValue(4.0))))
    );
  }

  @Test
  void testAutomaticWrapNumberInRealValue() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Real.Value(2.2).plus(-3).lessThan(5);
          }
        """,
        new ViolationsOf(new LessThan(new Plus(new RealValue(2.2), new RealValue(-3.0)), new RealValue(5.0)))
    );
  }

  //// TESTS FOR `Windows` CLASS API

  @Test
  void testDuring() {
    checkSuccessfulCompilation(
        // The following code will fail at constraint evaluation, but the Typescript should run just fine.
        // (due to the activity alias not being declared)
        """
          export default () => {
            return Windows.During("non existent activity");
          }
        """,
        new ViolationsOf(new During("non existent activity"))
    );
  }

  @Test
  void testStartOf() {
    checkSuccessfulCompilation(
        // The following code will fail at constraint evaluation, but the Typescript should run just fine.
        // (due to the activity alias not being declared)
        """
          export default () => {
            return Windows.StartOf("non existent activity");
          }
        """,
        new ViolationsOf(new StartOf("non existent activity"))
    );
  }

  @Test
  void testEndOf() {
    checkSuccessfulCompilation(
        // The following code will fail at constraint evaluation, but the Typescript should run just fine.
        // (due to the activity alias not being declared)
        """
          export default () => {
            return Windows.EndOf("non existent activity");
          }
        """,
        new ViolationsOf(new EndOf("non existent activity"))
    );
  }

  @Test
  void testIf() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Real.Resource("my_real_resource").lessThan(2)
              .if(Discrete.Resource("my_discrete_resource").changed());
          }
        """,
        new ViolationsOf(
            new Or(
                new Not(new Changed<>(
                    new ProfileExpression<>(new DiscreteResource("my_discrete_resource"))
                )),
                new LessThan(new RealResource("my_real_resource"), new RealValue(2.0))
            )
        )
    );
  }

  @Test
  void testAll() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Windows.All(
              Real.Resource("my_resource").lessThan(2),
              Discrete.Value("hello there").notEqual(Discrete.Value("hello there")),
              Real.Value(5).changed()
            );
          }
        """,
        new ViolationsOf(
            new And(
                java.util.List.of(
                    new LessThan(new RealResource("my_resource"), new RealValue(2.0)),
                    new NotEqual<>(new DiscreteValue(SerializedValue.of("hello there")), new DiscreteValue(SerializedValue.of("hello there"))),
                    new Changed<>(new ProfileExpression<>(new RealValue(5.0)))
                )
            )
        )
    );
  }

  @Test
  void testAny() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Windows.Any(
              Real.Resource("my_resource").lessThan(2),
              Discrete.Value("hello there").notEqual(Discrete.Value("hello there")),
              Real.Value(5).changed()
            );
          }
        """,
        new ViolationsOf(
            new Or(
                java.util.List.of(
                    new LessThan(new RealResource("my_resource"), new RealValue(2.0)),
                    new NotEqual<>(new DiscreteValue(SerializedValue.of("hello there")), new DiscreteValue(SerializedValue.of("hello there"))),
                    new Changed<>(new ProfileExpression<>(new RealValue(5.0)))
                )
            )
        )
    );
  }

  @Test
  void testNot() {
    checkSuccessfulCompilation(
        """
          export default () => {
            return Discrete.Resource("my_resource").changed().not()
          }
        """,
        new ViolationsOf(
            new Not(
                new Changed<>(new ProfileExpression<>(new DiscreteResource("my_resource")))
            )
        )
    );
  }

  @Test
  void testViolations() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Real.Resource("my_resource").equal(Real.Value(-1)).violations();
        }
        """,
        new ViolationsOf(new Equal<>(new RealResource("my_resource"), new RealValue(-1.0)))
    );
  }

  //// TESTS FOR `Constraint` API CLASS

  @Test
  void testForbiddenActivityOverlap() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Constraint.ForbiddenActivityOverlap("some activity", "some other activity")
        }
        """,
        new ForbiddenActivityOverlap("some activity", "some other activity")
    );
  }

  @Test
  void testForEachActivity() {
    checkSuccessfulCompilation(
        """
        export default () => {
          return Constraint.ForEachActivity(
            "activity type",
            "activity alias",
            Windows.During("activity alias")
          )
        }
        """,
        new ForEachActivity("activity type", "activity alias", new ViolationsOf(new During("activity alias")))
    );
  }
}
