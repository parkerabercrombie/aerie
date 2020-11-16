package gov.nasa.jpl.ammos.mpsa.aerie.merlin.protocol;

import java.util.Map;

public interface Adaptation<Activity extends ActivityInstance> {
  /* Produce */ Map<String, ActivityType<Activity>>
  /* Given   */ getActivityTypes();

  /* Produce */ Iterable<Activity>
  /* Given   */ getDaemons();

  /* Produce */ SimulationScope<?, ?, Activity>
  /* Given   */ createSimulationScope();
}