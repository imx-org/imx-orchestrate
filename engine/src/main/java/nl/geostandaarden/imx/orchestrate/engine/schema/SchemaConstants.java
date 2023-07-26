package nl.geostandaarden.imx.orchestrate.engine.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaConstants {

  public static final String QUERY_TYPE = "Query";

  public static final String QUERY_COLLECTION_SUFFIX = "Collection";

  public static final String QUERY_FILTER_SUFFIX = "Filter";

  public static final String QUERY_FILTER_ARGUMENTS = "filter";

  public static final String HAS_LINEAGE_FIELD = "hasLineage";
}
