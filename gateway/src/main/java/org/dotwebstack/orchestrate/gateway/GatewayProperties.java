package org.dotwebstack.orchestrate.gateway;


import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("orchestrate")
public class GatewayProperties {

  private String mapping;

  private Map<String, GatewaySource> sources;
}
