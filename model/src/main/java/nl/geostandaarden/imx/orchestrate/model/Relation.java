package nl.geostandaarden.imx.orchestrate.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public final class Relation extends AbstractRelation {

  private final String inverseName;

  @Builder.Default
  private final Multiplicity inverseMultiplicity = Multiplicity.MULTI;

  private final Map<String, Path> keyMapping;

  @Singular
  private final List<RelationFilterMapping> filterMappings;
}
