package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static java.util.Collections.unmodifiableSet;
import static org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser.SOURCE_MODELS_KEY;
import static org.dotwebstack.orchestrate.parser.yaml.YamlModelParser.INVALID_OBJECT_NODE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParserException;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelParserException;

public final class SourceModelDeserializer extends JsonDeserializer<Set<Model>> {

  @Override
  public Set<Model> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    if (node instanceof ObjectNode objectNode) {
      var models = new HashSet<Model>();

      objectNode.fields()
          .forEachRemaining(entry -> {
            try {
              models.add(context.readTreeAsValue(entry.getValue(), Model.class)
                  .toBuilder()
                  .alias(entry.getKey())
                  .build());
            } catch (IOException e) {
              throw new YamlModelParserException("Could not parse source model: " + entry.getKey(), e);
            }
          });

      return unmodifiableSet(models);
    }

    throw new YamlModelMappingParserException(String.format(INVALID_OBJECT_NODE, SOURCE_MODELS_KEY));
  }
}
