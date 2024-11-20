package nl.geostandaarden.imx.orchestrate.gateway;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("orchestrate")
public class GatewayProperties {

    private String mapping;

    private Map<String, GatewaySource> sources;
}
