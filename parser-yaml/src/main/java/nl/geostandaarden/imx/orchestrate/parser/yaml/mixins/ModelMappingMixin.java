package nl.geostandaarden.imx.orchestrate.parser.yaml.mixins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.parser.yaml.deserializers.SourceModelDeserializer;

public abstract class ModelMappingMixin {

  @JsonDeserialize(using = SourceModelDeserializer.class)
  private Set<Model> sourceModels;
}
