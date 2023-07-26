package nl.geostandaarden.imx.orchestrate.model.lineage;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder(toBuilder = true)
public class PathMapping {

  @Singular("addPath")
  private List<Path> path;
}
