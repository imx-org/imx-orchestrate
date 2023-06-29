package org.dotwebstack.orchestrate.gateway;

import java.util.Map;
import lombok.Data;

@Data
public class GatewaySource {

    private String type;

    private Map<String, Object> options;
}
