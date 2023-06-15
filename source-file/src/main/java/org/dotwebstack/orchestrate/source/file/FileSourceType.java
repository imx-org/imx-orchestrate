package org.dotwebstack.orchestrate.source.file;

import java.nio.file.Path;
import java.util.Map;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.SourceException;
import org.dotwebstack.orchestrate.source.SourceType;

public class FileSourceType implements SourceType {

  private static final String SOURCE_TYPE = "file";

  private static final String DATA_PATH_KEY = "dataPath";

  @Override
  public String getName() {
    return SOURCE_TYPE;
  }

  @Override
  public Source create(Model model, Map<String, Object> options) {
    validate(model, options);

    var dataPath = Path.of((String)options.get(DATA_PATH_KEY));
    return new FileSource(model, dataPath);
  }

  private void validate(Model model, Map<String, Object> options) {
    if (!options.containsKey(DATA_PATH_KEY)) {
      throw new SourceException(String.format("Config '%s' is missing.", DATA_PATH_KEY));
    }
    if (model == null) {
      throw new SourceException("Model can't be null.");
    }
  }
}