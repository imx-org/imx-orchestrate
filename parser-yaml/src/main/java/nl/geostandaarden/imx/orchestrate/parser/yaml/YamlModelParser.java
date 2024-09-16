package nl.geostandaarden.imx.orchestrate.parser.yaml;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueType;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.MultiplicityDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ModelDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ObjectTypeDeserializer;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.ValueTypeDeserializer;

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
        .addDeserializer(Multiplicity.class, new MultiplicityDeserializer());

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
