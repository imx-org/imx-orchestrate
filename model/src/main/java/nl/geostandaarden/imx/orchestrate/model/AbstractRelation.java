package nl.geostandaarden.imx.orchestrate.model;

import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class AbstractRelation extends AbstractProperty {

    protected final ObjectTypeRef target;

    private final Map<String, Path> keyMapping;

    public ObjectTypeRef getTarget(ObjectTypeRef parentTypeRef) {
        return target.getModelAlias() != null
                ? target
                : ObjectTypeRef.forType(parentTypeRef.getModelAlias(), target.getName());
    }
}
