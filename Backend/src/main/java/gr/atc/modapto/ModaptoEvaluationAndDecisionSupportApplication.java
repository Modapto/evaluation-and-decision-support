package gr.atc.modapto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ModaptoEvaluationAndDecisionSupportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModaptoEvaluationAndDecisionSupportApplication.class, args);
	}

}
