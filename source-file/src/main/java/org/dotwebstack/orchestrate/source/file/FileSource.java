package org.dotwebstack.orchestrate.source.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.Source;
import org.dotwebstack.orchestrate.source.SourceException;

public final class FileSource implements Source {

  private static final PathMatcher matcher = FileSystems.getDefault()
      .getPathMatcher("glob:**/*.json");

  private final Model model;

  private final ObjectMapper objectMapper = new JsonMapper();

  private final FileRepository fileRepository = new FileRepository();

  public FileSource(Model model, Path folderPath) {
    this.model = model;

    if (!Files.isDirectory(folderPath)) {
      throw new SourceException("File path does not exist or is not a directory.");
    }

    try (var filePaths = Files.list(folderPath)) {
      filePaths.filter(Files::isRegularFile)
          .filter(matcher::matches)
          .forEach(this::loadFile);
    } catch (IOException e) {
      throw new SourceException("Folder could not be read.", e);
    }
  }

  @Override
  public DataRepository getDataRepository() {
    return fileRepository;
  }

  private void loadFile(Path filePath) {
    try (var inputStream = Files.newInputStream(filePath)) {
      var typeName = FileUtils.getBaseName(filePath);
      var objectType = model.getObjectType(typeName);

      if (objectType == null) {
        throw new SourceException("Object type not found in model:" + typeName);
      }

      var data = objectMapper.readTree(inputStream);
      loadDocument(data, objectType);
    } catch (IOException e) {
      throw new SourceException("File could not be read.", e);
    }
  }

  private void loadDocument(JsonNode data, ObjectType objectType) {
    if (data instanceof ArrayNode arrayNode) {
      arrayNode.elements()
          .forEachRemaining(element -> loadObject(element, objectType));
      return;
    }


    throw new SourceException("Source data is not an array.");
  }

  private void loadObject(JsonNode data, ObjectType objectType) {
    if (data instanceof ObjectNode objectNode) {
      var objectKey = FileUtils.getObjectKey(objectNode, objectType);
      fileRepository.add(objectType.getName(), objectKey, objectNode);
      return;
    }

    throw new SourceException("Array element is not an object.");
  }
}
