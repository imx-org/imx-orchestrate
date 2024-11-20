package nl.geostandaarden.imx.orchestrate.ext.spatial;

public final class SpatialException extends RuntimeException {

    public SpatialException(String message) {
        super(message);
    }

    public SpatialException(String message, Throwable cause) {
        super(message, cause);
    }
}
