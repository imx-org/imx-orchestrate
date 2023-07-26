package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import static nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ComponentUtils.parseOptions;
import static nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ComponentUtils.parseType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.combiners.ResultCombiner;

public final class ResultCombinerDeserializer extends StdDeserializer<ResultCombiner> {

  @Serial
  private static final long serialVersionUID = 9062543896587214910L;

  private final transient ComponentRegistry componentRegistry;

  public ResultCombinerDeserializer(ComponentRegistry componentRegistry) {
    super(ResultCombiner.class);
    this.componentRegistry = componentRegistry;
  }

  @Override
  public ResultCombiner deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    return componentRegistry.createResultCombiner(parseType(node), parseOptions(node));
  }
}
