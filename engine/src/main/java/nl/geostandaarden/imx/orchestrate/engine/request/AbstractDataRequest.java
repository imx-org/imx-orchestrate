package nl.geostandaarden.imx.orchestrate.engine.request;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.engine.OrchestrateException;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.Relation;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDataRequest implements DataRequest {

  @Getter(AccessLevel.NONE)
  protected final Model model;

  protected final ObjectType objectType;

  protected final Set<SelectedProperty> selectedProperties;

  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public static abstract class Builder<B extends Builder<B>> {

    protected final Model model;

    protected final ObjectType objectType;

    protected final Set<SelectedProperty> selectedProperties = new LinkedHashSet<>();

    protected abstract B self();

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
            .nestedRequest(selectionFn.apply(ObjectRequest.builder(model, relation.getTarget())))
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
            .nestedRequest(selectionFn.apply(CollectionRequest.builder(model, relation.getTarget())))
            .build());
        return self();
      }

      throw new OrchestrateException("Child selection can only be applied on relation properties.");
    }
  }
}
