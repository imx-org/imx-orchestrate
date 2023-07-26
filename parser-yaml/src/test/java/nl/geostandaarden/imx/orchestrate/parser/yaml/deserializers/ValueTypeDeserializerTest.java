package nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.InstanceOfAssertFactories;
import nl.geostandaarden.imx.orchestrate.model.types.ValueType;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeFactory;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValueTypeDeserializerTest {

  private final ObjectMapper objectMapper = new YAMLMapper();

  private ValueTypeDeserializer deserializer;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private JsonParser parser;

  @Mock
  private DeserializationContext context;

  @BeforeEach
  void setUp() {
    deserializer = new ValueTypeDeserializer(new ValueTypeRegistry()
        .register(createValueTypeFactory()));
  }

  @Test
  void deserialize_returnsType_forStringInput() throws IOException {
    when(parser.getCodec().readTree(any())).thenReturn(TextNode.valueOf("Foo"));

    var valueType = deserializer.deserialize(parser, context);

    assertThat(valueType).isNotNull();
  }

  @Test
  void deserialize_returnsType_forObjectInputWithoutOptions() throws IOException {
    var objectNode = objectMapper.createObjectNode()
        .put("name", "Foo");
    when(parser.getCodec().readTree(any(JsonParser.class))).thenReturn(objectNode);

    var valueType = deserializer.deserialize(parser, context);

    assertThat(valueType).isNotNull()
        .isInstanceOf(FooType.class)
        .extracting("options")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .isEmpty();
  }

  @Test
  void deserialize_returnsType_forObjectInputWithOptions() throws IOException {
    var objectNode = objectMapper.createObjectNode()
        .put("name", "Foo");
    objectNode.putObject("options")
        .put("opt1", "val1")
        .put("opt2", 123);
    when(parser.getCodec().readTree(any(JsonParser.class))).thenReturn(objectNode);
    when(context.readTreeAsValue(any(ObjectNode.class), eq(Map.class)))
        .thenReturn(Map.of("opt1", "val1", "opt2", 123));

    var valueType = deserializer.deserialize(parser, context);

    assertThat(valueType).isNotNull()
        .isInstanceOf(FooType.class)
        .extracting("options")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("opt1", "val1")
        .containsEntry("opt2", 123);
  }

  @Getter
  @RequiredArgsConstructor
  private static class FooType implements ValueType {

    private final Map<String, Object> options;

    @Override
    public String getName() {
      return "Foo";
    }
  }

  private static ValueTypeFactory<FooType> createValueTypeFactory() {
    return new ValueTypeFactory<>() {
      @Override
      public String getTypeName() {
        return "Foo";
      }

      @Override
      public FooType create(Map<String, Object> options) {
        return new FooType(options);
      }
    };
  }
}
