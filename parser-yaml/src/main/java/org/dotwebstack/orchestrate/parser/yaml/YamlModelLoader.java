package org.dotwebstack.orchestrate.parser.yaml;

import com.google.auto.service.AutoService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;

@AutoService(ModelLoader.class)
public final class YamlModelLoader implements ModelLoader {

  @Override
  public String getName() {
    return "yaml";
  }

  @Override
  public Optional<Model> load(String alias, String location, ValueTypeRegistry valueTypeRegistry) {
    var modelParser = new YamlModelParser(valueTypeRegistry);

    try {
      var model = modelParser.parse(new FileInputStream(location))
          .toBuilder()
          .alias(alias)
          .build();

      return Optional.of(model);
    } catch (FileNotFoundException e) {
      throw new YamlModelParserException("Parsing model failed.", e);
    }
  }
}
