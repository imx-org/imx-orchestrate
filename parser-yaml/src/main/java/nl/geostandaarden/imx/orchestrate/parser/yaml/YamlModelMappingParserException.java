package nl.geostandaarden.imx.orchestrate.parser.yaml;

import java.io.Serial;

public final class YamlModelMappingParserException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1388266094581155230L;

    public YamlModelMappingParserException(String message) {
        super(message);
    }

    public YamlModelMappingParserException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
