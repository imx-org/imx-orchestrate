package nl.geostandaarden.imx.orchestrate.engine;

import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import nl.geostandaarden.imx.orchestrate.model.ComponentRegistry;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;

public interface OrchestrateExtension {

  default void enhanceSchema(TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQLCodeRegistry.Builder codeRegistryBuilder) {
  }

  default void registerComponents(ComponentRegistry componentRegistry) {
  }

  default void registerValueTypes(ValueTypeRegistry valueTypeRegistry) {
  }
}
