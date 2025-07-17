package gr.atc.modapto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class ModaptoEvaluationAndDecisionSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModaptoEvaluationAndDecisionSupportApplication.class, args);
	}

}
