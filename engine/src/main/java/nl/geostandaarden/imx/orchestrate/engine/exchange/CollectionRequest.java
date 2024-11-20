package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.filters.FilterExpression;

@Getter
public final class CollectionRequest extends AbstractDataRequest {

    private final FilterExpression filter;

    private CollectionRequest(
            Model model, ObjectType objectType, Set<SelectedProperty> selectedProperties, FilterExpression filter) {
        super(model, objectType, selectedProperties);
        this.filter = filter;
    }

    @Override
    public String toString() {
        return super.toString().concat(filter == null ? "" : "Filter: " + filter + "\n");
    }

    public static CollectionRequest.Builder builder(Model model) {
        return new Builder(model);
    }

    public static class Builder extends AbstractDataRequest.Builder<Builder> {

        private FilterExpression filter;

        private Builder(Model model) {
            super(model);
        }

        public Builder filter(FilterExpression filter) {
            this.filter = filter;
            return this;
        }

        public CollectionRequest build() {
            return new CollectionRequest(model, objectType, unmodifiableSet(selectedProperties), filter);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
