package nl.geostandaarden.imx.orchestrate.engine.request;

import static java.util.Collections.unmodifiableSet;

import java.util.Set;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;

public final class CollectionRequest extends AbstractDataRequest {

  private CollectionRequest(Model model, ObjectType objectType, Set<SelectedProperty> selectedProperties) {
    super(model, objectType, selectedProperties);
  }

  public static CollectionRequest.Builder builder(Model model, String typeName) {
    return new Builder(model, model.getObjectType(typeName));
  }

  public static CollectionRequest.Builder builder(Model model, ObjectTypeRef typeRef) {
    return new Builder(model, model.getObjectType(typeRef));
  }

  public static class Builder extends AbstractDataRequest.Builder<Builder> {

    private Builder(Model model, ObjectType objectType) {
      super(model, objectType);
    }

    public CollectionRequest build() {
      return new CollectionRequest(model, objectType, unmodifiableSet(selectedProperties));
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
