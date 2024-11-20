package nl.geostandaarden.imx.orchestrate.gateway.schema;

import static nl.geostandaarden.imx.orchestrate.model.Multiplicity.REQUIRED;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaUtils {

    public static Type<?> requiredType(String typeName) {
        return new NonNullType(new TypeName(typeName));
    }

    public static Type<?> requiredListType(String typeName) {
        return new NonNullType(new ListType(requiredType(typeName)));
    }

    public static Type<?> applyMultiplicity(Type<?> type, Multiplicity multiplicity) {
        if (!multiplicity.isSingular()) {
            return new ListType(new NonNullType(type));
        }

        if (multiplicity.equals(REQUIRED)) {
            return new NonNullType(type);
        }

        return type;
    }
}
