package gov.nasa.jpl.aerie.merlin.server.config;

import java.util.Objects;
import java.util.Optional;

public record AppConfiguration (
    int httpPort,
    JavalinLoggingState javalinLogging,
    Optional<String> missionModelConfigPath,
    String missionModelDataPath,
    Store store
) {
  public AppConfiguration {
    Objects.requireNonNull(javalinLogging);
    Objects.requireNonNull(missionModelConfigPath);
    Objects.requireNonNull(missionModelDataPath);
    Objects.requireNonNull(store);
  }
}