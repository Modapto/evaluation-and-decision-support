package gr.atc.modapto.config;

import gr.atc.modapto.util.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${dt.management.url}")
    private String dtmUrl;

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(dtmUrl)
                .requestInterceptor(new LoggingInterceptor())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
