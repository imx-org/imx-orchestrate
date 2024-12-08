package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.engine.exchange.ObjectRequest;

@Getter
@SuperBuilder(toBuilder = true)
public final class ObjectNode extends AbstractCompoundNode {

    private final Map<String, Object> objectKey;

    public ObjectRequest toRequest() {
        return ObjectRequest.builder().selection(this).build();
    }
}
