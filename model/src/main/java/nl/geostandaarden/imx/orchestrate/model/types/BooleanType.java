package nl.geostandaarden.imx.orchestrate.model.types;

import lombok.ToString;

@ToString
public final class BooleanType implements ScalarType<Boolean> {

    @Override
    public Class<Boolean> getJavaType() {
        return Boolean.class;
    }

    @Override
    public String getName() {
        return Boolean.class.getSimpleName();
    }
}
