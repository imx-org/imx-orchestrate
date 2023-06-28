package org.dotwebstack.orchestrate.parser.yaml.mixins;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Set;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.parser.yaml.deserializers.SourceModelDeserializer;

public abstract class ModelMappingMixin {

  @JsonDeserialize(using = SourceModelDeserializer.class)
  private Set<Model> sourceModels;
}
