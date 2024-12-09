package nl.geostandaarden.imx.orchestrate.engine.selection;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.engine.exchange.CollectionRequest;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;

@Getter
@SuperBuilder(toBuilder = true)
public final class CollectionNode extends AbstractCompoundNode {

    private final FilterExpression filter;

    public CollectionRequest toRequest() {
        return CollectionRequest.builder() //
                .selection(this)
                .build();
    }
}
