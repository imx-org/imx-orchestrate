package nl.geostandaarden.imx.orchestrate.model.types;

public interface ScalarType<T> extends ValueType {

    Class<T> getJavaType();
}
