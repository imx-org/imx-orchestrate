package org.dotwebstack.orchestrate.source;

import org.dotwebstack.orchestrate.model.Model;

import java.util.Map;

public interface SourceType {

    String getName();

    Source create(Model model, Map<String, Object> options);
}
