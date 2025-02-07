package gr.atc.modapto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Value("${build.version}")
    private String appVersion;

    @Bean
    public OpenAPI openAPIDocumentation() {
        return new OpenAPI()
                .info(new Info()
                    .title("Evaluation and Decision Support API")
                    .version(appVersion)
                    .description("API documentation for EDS Service of MODAPTO project"))
                .openapi("3.0.3");
    }
}