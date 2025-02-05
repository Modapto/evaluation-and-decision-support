package gr.atc.modapto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPIDocumentation() {
        return new OpenAPI()
                .specVersion(SpecVersion.V30)
                .info(new Info()
                    .title("Evaluation and Decision Support API")
                    .version("1.0")
                    .description("API documentation for EDS Service of MODAPTO project"));
    }
}