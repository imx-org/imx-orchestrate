package org.dotwebstack.orchestrate.parser.yaml.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "name", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
public abstract class CombinerMixin {
}
