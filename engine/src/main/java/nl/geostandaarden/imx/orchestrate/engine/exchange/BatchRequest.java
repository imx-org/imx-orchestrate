package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
public final class BatchRequest extends AbstractDataRequest {

  private final Collection<Map<String, Object>> objectKeys;

  private BatchRequest(ObjectType objectType, Set<SelectedProperty> selectedProperties, Collection<Map<String, Object>> objectKeys) {
    super(objectType, selectedProperties);
    this.objectKeys = objectKeys;
  }

  @Override
  public String toString() {
    return super.toString()
        .concat("Object keys: " + objectKeys + "\n");
  }

  public static BatchRequest.Builder builder(Model model) {
    return new Builder(model);
  }

  public static class Builder extends AbstractDataRequest.Builder<Builder> {

    private Collection<Map<String, Object>> objectKeys = new LinkedHashSet<>();

    private Builder(Model model) {
      super(model);
    }

    public Builder objectKeys(Collection<Map<String, Object>> objectKeys) {
      this.objectKeys = objectKeys;
      return this;
    }

    public Builder objectKey(Map<String, Object> objectKey) {
      objectKeys.add(objectKey);
      return this;
    }

    public BatchRequest build() {
      return new BatchRequest(objectType, unmodifiableSet(selectedProperties), unmodifiableCollection(objectKeys));
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
