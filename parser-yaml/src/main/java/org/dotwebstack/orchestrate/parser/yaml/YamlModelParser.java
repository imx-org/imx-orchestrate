package org.dotwebstack.orchestrate.parser.yaml;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.dotwebstack.orchestrate.model.types.ValueType;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.CardinalityDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ModelDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ObjectTypeDeserializer;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.ValueTypeDeserializer;

public final class YamlModelParser {

  public static final String OBJECT_TYPES_KEY = "objectTypes";

  public static final String ATTRIBUTES_KEY = "attributes";

  public static final String RELATIONS_KEY = "relations";

  public static final String INVALID_OBJECT_NODE = "Property '%s' is not an object node.";

  public static final String INVALID_TEXT_NODE = "Property '%s' is not a text node.";

  public static final String INVALID_TEXT_OR_INT_NODE = "Property '%s' is not a text or int node.";

  private final YAMLMapper yamlMapper = new YAMLMapper();

  public YamlModelParser(ValueTypeRegistry valueTypeRegistry) {
    var module = new SimpleModule()
        .addDeserializer(Model.class, new ModelDeserializer())
        .addDeserializer(ObjectType.class, new ObjectTypeDeserializer())
        .addDeserializer(ValueType.class, new ValueTypeDeserializer(valueTypeRegistry))
        .addDeserializer(Cardinality.class, new CardinalityDeserializer());

    yamlMapper.registerModule(module);
  }

  public Model parse(InputStream inputStream) {
    try {
      return yamlMapper.readValue(inputStream, Model.class);
    } catch (IOException e) {
      throw new YamlModelParserException(String.format("An error occurred while processing model:%n%s",
          e.getMessage()), e);
    }
  }
}
