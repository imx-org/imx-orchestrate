package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.engine.selection.BatchNode;

@Getter
@ToString
@Builder(toBuilder = true)
public final class BatchRequest implements DataRequest<BatchNode> {

    private final BatchNode selection;
}
