package com.tcon.financial_service.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing  // ✅ Only one @EnableMongoAuditing in entire project
@EnableMongoRepositories(basePackages = "com.tcon.financial_service")
public class MongoConfig {
    // No additional beans needed if using application.yml configuration
}
