package nl.geostandaarden.imx.orchestrate.engine.exchange;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.engine.selection.CollectionNode;

@Getter
@ToString
@Builder(toBuilder = true)
public final class CollectionRequest implements DataRequest<CollectionNode> {

    private final CollectionNode selection;
}
