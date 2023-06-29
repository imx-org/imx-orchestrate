package org.dotwebstack.orchestrate.engine;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

public interface OrchestrateExtension {

  default void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQLCodeRegistry.Builder codeRegistryBuilder) {
  }

  default void registerComponents(ComponentRegistry componentRegistry) {
  }

  default void registerValueTypes(ValueTypeRegistry valueTypeRegistry) {
  }
}
