package gr.atc.modapto.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("!test")
@EnableElasticsearchRepositories(basePackages = "gr.atc.modapto.repository")
public class ElasticsearchConfig {
}