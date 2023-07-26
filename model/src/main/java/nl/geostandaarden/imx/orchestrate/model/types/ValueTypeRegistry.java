package nl.geostandaarden.imx.orchestrate.model.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;

public final class ValueTypeRegistry {

  private final Map<String, ValueTypeFactory<?>> valueTypeFactories = new HashMap<>();

  public ValueTypeRegistry() {
    register(newFactory(ScalarTypes.BOOLEAN), newFactory(ScalarTypes.DOUBLE), newFactory(ScalarTypes.FLOAT),
        newFactory(ScalarTypes.INTEGER), newFactory(ScalarTypes.LONG), newFactory(ScalarTypes.STRING));
  }

  public ValueTypeRegistry register(ValueTypeFactory<?>... valueTypeFactories) {
    Arrays.stream(valueTypeFactories).forEach(valueTypeFactory ->
        this.valueTypeFactories.put(valueTypeFactory.getTypeName(), valueTypeFactory));
    return this;
  }

  public ValueTypeFactory<?> getValueTypeFactory(String typeName) {
    try {
      return valueTypeFactories.get(typeName);
    } catch (NullPointerException e) {
      throw new ModelException("Unknown value type: " + typeName, e);
    }
  }

  private static ValueTypeFactory<?> newFactory(ValueType valueType) {
    return new ValueTypeFactory<>() {
      @Override
      public String getTypeName() {
        return valueType.getName();
      }

      @Override
      public ValueType create(Map<String, Object> options) {
        return valueType;
      }
    };
  }
}
