package nl.geostandaarden.imx.orchestrate.model.types;

import lombok.ToString;

@ToString
public final class IntegerType implements ScalarType<Integer> {

    @Override
    public Class<Integer> getJavaType() {
        return Integer.class;
    }

    @Override
    public String getName() {
        return Integer.class.getSimpleName();
    }
}
