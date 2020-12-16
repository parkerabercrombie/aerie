package gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.states;

import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.Module;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.RealResource;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.ResourcesBuilder;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.framework.models.LinearIntegrationModel;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.protocol.Condition;
import gov.nasa.jpl.ammos.mpsa.aerie.merlin.timeline.Query;

public final class LinearIntegrationModule<$Schema> extends Module<$Schema> {
  private final Query<$Schema, Double, LinearIntegrationModel> query;

  public final Volume volume;
  public final Rate rate;

  public LinearIntegrationModule(final ResourcesBuilder.Cursor<$Schema> builder) {
    super(builder);

    this.query = builder.model(new LinearIntegrationModel(0.0, 0.0), ev -> ev);
    this.volume = new Volume(builder.real("volume", now -> now.ask(this.query).getVolume()));
    this.rate = new Rate(builder.real("rate", now -> now.ask(this.query).getRate()));
  }

  public final class Volume {
    public final RealResource<$Schema> resource;

    private Volume(final RealResource<$Schema> resource) {
      this.resource = resource;
    }

    public double get() {
      return this.resource.ask(now());
    }

    public Condition<$Schema> isBetween(final double lower, final double upper) {
      // TODO
      return null;
    }
  }

  public final class Rate {
    public final RealResource<$Schema> resource;

    private Rate(final RealResource<$Schema> resource) {
      this.resource = resource;
    }

    public double get() {
      return this.resource.ask(now());
    }

    public void add(final double delta) {
      emit(delta, query);
    }

    public Condition<$Schema> isBetween(final double lower, final double upper) {
      // TODO
      return null;
    }
  }
}
