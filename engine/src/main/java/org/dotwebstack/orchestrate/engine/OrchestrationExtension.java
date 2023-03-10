package org.dotwebstack.orchestrate.engine;

import com.fasterxml.jackson.databind.Module;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface OrchestrationExtension {

  default void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLCodeRegistry.Builder codeRegistryBuilder) {}

  default Module getLineageSerializerModule() {
    return null;
  }
}
