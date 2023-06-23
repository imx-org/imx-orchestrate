package org.dotwebstack.gateway;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewaySource {

    private String type;

    private Map<String, Object> options;
}
