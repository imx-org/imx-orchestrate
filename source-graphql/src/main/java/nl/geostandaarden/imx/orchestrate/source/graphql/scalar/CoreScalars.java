package nl.geostandaarden.imx.orchestrate.source.graphql.scalar;

import graphql.schema.GraphQLScalarType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreScalars {

  public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
      .name("Date")
      .description("Date type")
      .coercing(new DateCoercing())
      .build();

  public static final GraphQLScalarType DATETIME = GraphQLScalarType.newScalar()
      .name("DateTime")
      .description("DateTime type")
      .coercing(new DateTimeCoercing())
      .build();
}
