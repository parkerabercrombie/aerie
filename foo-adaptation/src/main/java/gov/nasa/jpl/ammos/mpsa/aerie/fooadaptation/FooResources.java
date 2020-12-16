package gov.nasa.jpl.ammos.mpsa.aerie.fooadaptation;

import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.RealResource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.ResourcesBuilder;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.states.LinearIntegrationModule;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.states.RegisterModule;
import gov.nasa.jpl.ammos.mpsa.aerie.fooadaptation.generated.Module;

public final class FooResources<$Schema> extends Module<$Schema> {
  // Need a clear story for how to logically group resource questions and event emissions together.
  // Need a way to produce a condition for a resource.
  // Need a way to assemble conditions into an overall constraint.
  // Need a way to extract constraints from an adaptation.
  // Need a way to pose constraints against activities, and generally modeling activity behavior with resources.
  // Need to support waitUntil(condition) (which ought to subsume waitFor(activity)).
  // Need to port the breadcrumb-based replaying tasks to this framework, so that activities can be written
  //   in direct style (rather than continuation-passing or state-machine style).
  // Need a clear story for external models.
  // Need to collect profiles from published resources as simulation proceeds.
  // Need to generalize RealDynamics to nonlinear polynomials.
  // Need to use a more representative event type for the sample.
  // Need to implement compile-time code generation for various aspects of the Framework.

  public final RegisterModule<$Schema, Double> foo;
  public final LinearIntegrationModule<$Schema> data;

  public final RealResource<$Schema> combo;

  public FooResources(final ResourcesBuilder.Cursor<$Schema> builder) {
    super(builder);

    this.foo = RegisterModule.create(builder.descend("foo"), 0.0);
    this.data = new LinearIntegrationModule<>(builder.descend("data"));
    this.combo = this.data.volume.resource.plus(this.data.rate.resource);

    // TODO: automatically perform this for each @Daemon annotation
    builder.daemon("test", this::test);
  }

  public void test() {
    foo.set(21.0);
    data.rate.add(42.0);
  }
}
