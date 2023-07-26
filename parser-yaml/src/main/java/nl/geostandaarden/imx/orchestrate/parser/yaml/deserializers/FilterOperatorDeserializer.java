package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterOperator;

public final class FilterOperatorDeserializer extends StdDeserializer<FilterOperator> {

  @Serial
  private static final long serialVersionUID = 4459661171812164269L;

  private final transient ComponentRegistry componentRegistry;

  public FilterOperatorDeserializer(ComponentRegistry componentRegistry) {
    super(FilterOperator.class);
    this.componentRegistry = componentRegistry;
  }

  @Override
  public FilterOperator deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    return componentRegistry.createFilterOperator(ComponentUtils.parseType(node), ComponentUtils.parseOptions(node));
  }
}
