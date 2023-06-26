package org.dotwebstack.orchestrate.engine;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dotwebstack.orchestrate.model.ComponentRegistry;

public interface OrchestrateExtension {

  default void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQLCodeRegistry.Builder codeRegistryBuilder) {
  }

  default void registerComponents(ComponentRegistry componentRegistry) {
  }
}
