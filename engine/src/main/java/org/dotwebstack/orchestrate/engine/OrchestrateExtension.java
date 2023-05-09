package org.dotwebstack.orchestrate.engine;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;

public interface OrchestrateExtension {

  default void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry, GraphQLCodeRegistry.Builder codeRegistryBuilder) {}
}
