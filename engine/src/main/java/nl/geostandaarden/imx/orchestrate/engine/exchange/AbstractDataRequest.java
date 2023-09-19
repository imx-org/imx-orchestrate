package nl.geostandaarden.imx.orchestrate.engine.exchange;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Relation;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDataRequest implements DataRequest {

  protected final Model model;

  protected final ObjectType objectType;

  protected final Set<SelectedProperty> selectedProperties;

  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static abstract class Builder<B extends Builder<B>> {

    protected final Model model;

    protected ObjectType objectType;

    protected Set<SelectedProperty> selectedProperties = new LinkedHashSet<>();

    protected abstract B self();

    public B objectType(String name) {
      objectType = model.getObjectType(name);
      return self();
    }

    public B objectType(ObjectTypeRef typeRef) {
      objectType = model.getObjectType(typeRef.getName());
      return self();
    }

    public B selectedProperties(Set<SelectedProperty> selectedProperties) {
      this.selectedProperties = selectedProperties;
      return self();
    }

    public B selectProperty(String name) {
      selectedProperties.add(SelectedProperty.builder()
          .property(objectType.getProperty(name))
          .build());
      return self();
    }

    public B selectObjectProperty(String name, Function<ObjectRequest.Builder, DataRequest> selectionFn) {
      var property = objectType.getProperty(name);

      if (property instanceof Relation relation) {
        selectedProperties.add(SelectedProperty.builder()
            .property(property)
            .nestedRequest(selectionFn.apply(ObjectRequest.builder(model)
                .objectType(relation.getTarget())))
            .build());
        return self();
      }

      throw new OrchestrateException("Child selection can only be applied on relation properties.");
    }

    public B selectCollectionProperty(String name, Function<CollectionRequest.Builder, DataRequest> selectionFn) {
      var property = objectType.getProperty(name);

      if (property instanceof Relation relation) {
        selectedProperties.add(SelectedProperty.builder()
            .property(property)
            .nestedRequest(selectionFn.apply(CollectionRequest.builder(model).objectType(relation.getTarget())))
            .build());
        return self();
      }

      throw new OrchestrateException("Child selection can only be applied on relation properties.");
    }
  }
}
