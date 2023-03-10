package org.dotwebstack.orchestrate.engine.schema;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaUtils {

  public static Type<?> requiredType(String typeName) {
    return new NonNullType(new TypeName(typeName));
  }

  public static Type<?> requiredListType(String typeName) {
    return new NonNullType(new ListType(requiredType(typeName)));
  }
}
