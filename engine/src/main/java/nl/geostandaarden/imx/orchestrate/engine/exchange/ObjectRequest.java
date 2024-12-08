package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;

@Getter
@ToString
@Builder(toBuilder = true)
public final class ObjectRequest implements DataRequest<ObjectNode> {

    private final ObjectNode selection;
}
