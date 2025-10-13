package gr.atc.modapto;

import gr.atc.modapto.config.properties.KeycloakProperties;
import gr.atc.modapto.config.properties.SmartServiceDebugProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableConfigurationProperties({KeycloakProperties.class, SmartServiceDebugProperties.class})
public class ModaptoEvaluationAndDecisionSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModaptoEvaluationAndDecisionSupportApplication.class, args);
	}

}
