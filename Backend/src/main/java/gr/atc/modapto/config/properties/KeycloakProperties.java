package gr.atc.modapto.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties (

    String tokenUri,
    String clientId,
    String clientSecret
){}
