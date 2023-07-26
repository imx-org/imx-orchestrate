package nl.geostandaarden.imx.orchestrate.model.combiners;

import java.util.Map;

public interface ResultCombinerType {

  String getName();

  ResultCombiner create(Map<String, Object> options);
}
