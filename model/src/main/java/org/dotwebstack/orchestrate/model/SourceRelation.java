package org.dotwebstack.orchestrate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(toBuilder = true)
public final class SourceRelation {

  private final ObjectTypeRef sourceType;

  private final Relation property;
}
