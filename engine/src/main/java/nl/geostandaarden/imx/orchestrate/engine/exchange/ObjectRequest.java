package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.engine.selection.ObjectNode;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
public final class ObjectRequest extends AbstractDataRequest {

    private final ObjectNode selection;

    private final Map<String, Object> objectKey;

    private ObjectRequest(
            Model model,
            ObjectType objectType,
            Set<SelectedProperty> selectedProperties,
            ObjectNode selection,
            Map<String, Object> objectKey) {
        super(model, objectType, selectedProperties);
        this.selection = selection;
        this.objectKey = objectKey;
    }

    public ObjectType getObjectType() {
        return selection != null ? selection.getObjectType() : objectType;
    }

    @Override
    public String toString() {
        return super.toString().concat("Object key: " + objectKey + "\n");
    }

    public static ObjectRequest.Builder builder(Model model) {
        return new Builder(model);
    }

    public static class Builder extends AbstractDataRequest.Builder<Builder> {

        private ObjectNode selection;

        private Map<String, Object> objectKey;

        private Builder(Model model) {
            super(model);
        }

        public Builder selection(ObjectNode selection) {
            this.selection = selection;
            return self();
        }

        public Builder objectKey(Map<String, Object> objectKey) {
            this.objectKey = objectKey;
            return this;
        }

        public ObjectRequest build() {
            return new ObjectRequest(model, objectType, unmodifiableSet(selectedProperties), selection, objectKey);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
