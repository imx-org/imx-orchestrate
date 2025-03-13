package nl.geostandaarden.imx.orchestrate.parser.yaml.mixins;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import nl.geostandaarden.imx.orchestrate.model.PathMapping;

public abstract class ConditionalWhenMixin {

    @JsonAlias("pathMappings")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<PathMapping> pathMappings;
}
