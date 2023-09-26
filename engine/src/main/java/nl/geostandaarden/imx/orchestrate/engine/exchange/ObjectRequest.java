package nl.geostandaarden.imx.orchestrate.engine.exchange;

import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;

@Getter
@ToString
public final class ObjectRequest extends AbstractDataRequest {

  private final Map<String, Object> objectKey;

  private ObjectRequest(Model model, ObjectType objectType, Set<SelectedProperty> selectedProperties, Map<String, Object> objectKey) {
    super(model, objectType, selectedProperties);
    this.objectKey = objectKey;
  }

  public static ObjectRequest.Builder builder(Model model) {
    return new Builder(model);
  }

  public static class Builder extends AbstractDataRequest.Builder<Builder> {

    private Map<String, Object> objectKey;

    private Builder(Model model) {
      super(model);
    }

    public Builder objectKey(Map<String, Object> objectKey) {
      this.objectKey = objectKey;
      return this;
    }

    public ObjectRequest build() {
      return new ObjectRequest(model, objectType, unmodifiableSet(selectedProperties), objectKey);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
