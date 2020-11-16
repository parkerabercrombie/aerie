package gov.nasa.jpl.ammos.mpsa.aerie.merlin.protocol;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.real.RealCondition;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.resources.real.RealDynamics;

import java.util.Objects;
import java.util.Set;

public abstract class SolvableDynamics<ResourceType, ConditionType> {
  public abstract ResourceType solve(final Visitor visitor);

  public interface Visitor {
    Double real(final RealDynamics dynamics);

    <ResourceType>
    ResourceType discrete(final ResourceType fact);
  }

  public static SolvableDynamics<Double, RealCondition>
  real(final RealDynamics dynamics)
  {
    Objects.requireNonNull(dynamics);

    return new SolvableDynamics<>() {
      @Override
      public Double solve(final Visitor visitor) {
        return visitor.real(dynamics);
      }
    };
  }

  public static <ResourceType>
  SolvableDynamics<ResourceType, Set<ResourceType>>
  discrete(final ResourceType fact)
  {
    Objects.requireNonNull(fact);

    return new SolvableDynamics<>() {
      @Override
      public ResourceType solve(final Visitor visitor) {
        return visitor.discrete(fact);
      }
    };
  }
}