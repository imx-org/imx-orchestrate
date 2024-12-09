package nl.geostandaarden.imx.orchestrate.engine.selection;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import nl.geostandaarden.imx.orchestrate.engine.exchange.BatchRequest;

@Getter
@SuperBuilder(toBuilder = true)
public final class BatchNode extends AbstractCompoundNode {

    @Singular
    private final Collection<Map<String, Object>> objectKeys;

    public BatchRequest toRequest() {
        return BatchRequest.builder() //
                .selection(this)
                .build();
    }
}
