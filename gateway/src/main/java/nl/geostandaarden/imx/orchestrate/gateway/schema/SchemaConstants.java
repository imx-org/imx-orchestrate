package nl.geostandaarden.imx.orchestrate.gateway.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaConstants {

  public static final String QUERY_TYPE = "Query";

  public static final String QUERY_COLLECTION_SUFFIX = "Collection";

  public static final String QUERY_BATCH_SUFFIX = "Batch";

  public static final String QUERY_FILTER_SUFFIX = "Filter";

  public static final String QUERY_FILTER_ARGUMENT = "filter";

  public static final String KEY_TYPE_SUFFIX = "Key";

  public static final String BATCH_KEYS_ARG = "keys";

  public static final String HAS_LINEAGE_FIELD = "hasLineage";
}
