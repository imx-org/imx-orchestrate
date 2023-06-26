package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseOptions;
import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.matchers.Matcher;

public final class MatcherDeserializer extends StdDeserializer<Matcher> {

  @Serial
  private static final long serialVersionUID = 7898902256110027311L;

  private final transient ComponentRegistry componentRegistry;

  public MatcherDeserializer(ComponentRegistry componentRegistry) {
    super(Matcher.class);
    this.componentRegistry = componentRegistry;
  }

  @Override
  public Matcher deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    return componentRegistry.createMatcher(parseType(node), parseOptions(node));
  }
}
