package gov.nasa.jpl.ammos.mpsa.aerie.simulation.utils;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.typemappers.ParameterMapper;

import java.util.HashMap;
import java.util.Map;

public final class TypeRegistry {
  // INVARIANT: For every key `Class<T>` in `map`, the associated value implements `ParameterMapper<T>`.
  private final Map<Class<?>, ParameterMapper<?>> map = new HashMap<>();

  public <T> void put(final Class<T> klass, final ParameterMapper<T> mapper) {
    // SAFETY: The invariant on `this.map` is sustained. The method signature guarantees that, for every T,
    // a given `Class<T>` is associated with a given `ParameterMapper<T>`.
    this.map.put(klass, mapper);
  }

  public <T> ParameterMapper<T> get(final Class<T> klass) {
    // SAFETY: The invariant on `this.map` ensures that this cast is valid.
    @SuppressWarnings("unchecked")
    final var result = (ParameterMapper<T>)this.map.get(klass);
    return result;
  }

  public <T> ParameterMapper<T> getOrDefault(final Class<T> klass, final ParameterMapper<T> defaultValue) {
    // SAFETY: The invariant on `this.map` ensures that this cast is valid.
    @SuppressWarnings("unchecked")
    final var result = (ParameterMapper<T>)this.map.getOrDefault(klass, defaultValue);
    return result;
  }
}
