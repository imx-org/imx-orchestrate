package org.dotwebstack.orchestrate;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class GatewaySource {

    private String type;

    private Map<String, Object> options;
}
