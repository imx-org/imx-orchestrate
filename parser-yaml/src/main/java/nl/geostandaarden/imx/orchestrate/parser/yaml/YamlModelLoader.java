package nl.geostandaarden.imx.orchestrate.parser.yaml;

import com.google.auto.service.AutoService;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoader;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

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
