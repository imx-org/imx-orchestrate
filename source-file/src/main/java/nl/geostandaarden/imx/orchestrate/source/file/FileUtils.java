package nl.geostandaarden.imx.orchestrate.source.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.selection.TreeNode;
import nl.geostandaarden.imx.orchestrate.engine.source.SourceException;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Property;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String getBaseName(Path filePath) {
        var fileName = filePath.getFileName().toString();

        var dotIndex = fileName.lastIndexOf(".");

        if (dotIndex == -1) {
            throw new IllegalArgumentException("File path does not have an extension.");
        }

        return fileName.substring(0, dotIndex);
    }

    public static Map<String, Object> getObjectKey(ObjectNode objectNode, ObjectType objectType) {
        var propertyNames = objectType.getIdentityProperties().stream()
                .map(Property::getName)
                .toList();

        return objectNodeToMap(objectNode.deepCopy().retain(propertyNames));
    }

    public static Map<String, Object> getObjectProperties(ObjectNode objectNode, Map<String, TreeNode> childNodes) {
        var propertyNames = childNodes.keySet();
        return objectNodeToMap(objectNode.deepCopy().retain(propertyNames));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectNodeToMap(ObjectNode objectNode) {
        try {
            return OBJECT_MAPPER.treeToValue(objectNode, Map.class);
        } catch (JsonProcessingException e) {
            throw new SourceException("Error while procession object.", e);
        }
    }
}
