package org.dotwebstack.orchestrate.parser.yaml;

import com.google.auto.service.AutoService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

@AutoService(ModelLoader.class)
public final class YamlModelLoader implements ModelLoader {

  @Override
  public String getName() {
    return "yaml";
  }

  @Override
  public Model load(String location, ValueTypeRegistry valueTypeRegistry) {
    var modelParser = new YamlModelParser(valueTypeRegistry);

    try {
      return modelParser.parse(new FileInputStream(location));
    } catch (FileNotFoundException e) {
      throw new YamlModelParserException("Parsing model failed.", e);
    }
  }
}
