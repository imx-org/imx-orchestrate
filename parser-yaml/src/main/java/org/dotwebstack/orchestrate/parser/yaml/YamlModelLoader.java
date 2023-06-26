package org.dotwebstack.orchestrate.parser.yaml;

import com.google.auto.service.AutoService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;

@AutoService(ModelLoader.class)
public final class YamlModelLoader implements ModelLoader {

  private final YamlModelParser modelParser = YamlModelParser.getInstance();

  @Override
  public String getName() {
    return "yaml";
  }

  @Override
  public Optional<Model> loadModel(String alias, String location) {
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
