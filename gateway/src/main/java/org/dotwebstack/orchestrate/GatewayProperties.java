package org.dotwebstack.orchestrate;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties("orchestrate.gateway")
public class GatewayProperties {

  private TestFixtures.TargetModelType targetModel;

  private String mapping;

  private Map<String, GatewaySource> sources;
}
