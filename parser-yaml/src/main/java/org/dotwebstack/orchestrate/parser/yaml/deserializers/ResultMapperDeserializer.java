package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseOptions;
import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.mappers.ResultMapper;

public final class ResultMapperDeserializer extends StdDeserializer<ResultMapper> {

  @Serial
  private static final long serialVersionUID = 4459661171812164269L;

  private final transient ComponentRegistry componentRegistry;

  public ResultMapperDeserializer(ComponentRegistry componentRegistry) {
    super(ResultMapper.class);
    this.componentRegistry = componentRegistry;
  }

  @Override
  public ResultMapper deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    return componentRegistry.createResultMapper(parseType(node), parseOptions(node));
  }
}
