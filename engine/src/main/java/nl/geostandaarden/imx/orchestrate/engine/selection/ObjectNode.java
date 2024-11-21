package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public final class ObjectNode extends CompoundNode {

    private final Map<String, Object> objectKey;
}
