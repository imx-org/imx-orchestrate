package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.AbstractRelation;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Property;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectedProperty {

    private final Property property;

    private final DataRequest nestedRequest;

    public String getName() {
        return property.getName();
    }

    @Override
    public String toString() {
        return property.getName();
    }

    public static SelectedProperty forProperty(Property property) {
        return forProperty(property, null);
    }

    public static SelectedProperty forProperty(Property property, DataRequest nestedRequest) {
        if (property instanceof Attribute && nestedRequest != null) {
            throw new OrchestrateException("Attribute properties can not have a nested request.");
        }

        if (property instanceof AbstractRelation && nestedRequest == null) {
            throw new OrchestrateException("Relation properties require a nested request.");
        }

        return new SelectedProperty(property, nestedRequest);
    }
}
