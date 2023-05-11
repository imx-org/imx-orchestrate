package org.dotwebstack.orchestrate.parser.yaml.deserializers;

import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseOptions;
import static org.dotwebstack.orchestrate.parser.yaml.deserializers.ComponentUtils.parseType;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.io.Serial;
import org.dotwebstack.orchestrate.model.ComponentFactory;
import org.dotwebstack.orchestrate.model.matchers.ResultMatcher;

public final class ResultMatcherDeserializer extends StdDeserializer<ResultMatcher> {

  @Serial
  private static final long serialVersionUID = 9062543896587214910L;

  private final transient ComponentFactory componentFactory;

  public ResultMatcherDeserializer(ComponentFactory componentFactory) {
    super(ResultMatcher.class);
    this.componentFactory = componentFactory;
  }

  @Override
  public ResultMatcher deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    var node = parser.getCodec()
        .readTree(parser);

    return componentFactory.createResultMatcher(parseType(node), parseOptions(node));
  }
}
