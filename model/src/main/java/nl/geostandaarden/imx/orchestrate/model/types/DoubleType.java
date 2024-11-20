package nl.geostandaarden.imx.orchestrate.model.types;

import lombok.ToString;

@ToString
public final class DoubleType implements ScalarType<Double> {

    @Override
    public Class<Double> getJavaType() {
        return Double.class;
    }

    @Override
    public String getName() {
        return Double.class.getSimpleName();
    }
}
