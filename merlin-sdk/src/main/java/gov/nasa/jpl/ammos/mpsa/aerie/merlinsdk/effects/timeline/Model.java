package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.timeline;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.effects.EffectTrait;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Duration;

public interface Model<Effect, Self extends Model<Effect, Self>> {
  Self duplicate();

  EffectTrait<Effect> effectTrait();

  void react(Effect effect);

  default void step(final Duration duration) {
    // Unless specified, a model is unaffected by the passage of time.
  }
}