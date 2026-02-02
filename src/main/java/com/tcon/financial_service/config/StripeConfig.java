package com.tcon.financial_service.config;

import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${payment.stripe.api-key}")
    private String stripeApiKey;

    @Value("${payment.stripe.api-version:2023-10-16}")
    private String apiVersion;

    @PostConstruct
    public void init() {
        try {
            if (stripeApiKey == null || stripeApiKey.trim().isEmpty()) {
                throw new IllegalStateException("Stripe API key is not configured");
            }

            Stripe.apiKey = stripeApiKey;

            log.info("✅ Stripe initialized successfully");

            if (stripeApiKey.length() >= 10) {
                log.info("API Key: {}****", stripeApiKey.substring(0, 10));
            }

            log.info("API Version: {} (Set in Stripe Dashboard)", apiVersion);

        } catch (Exception e) {
            log.error("❌ Failed to initialize Stripe", e);
            throw new IllegalStateException("Stripe initialization failed", e);
        }
    }

    @Bean
    public RequestOptions stripeRequestOptions() {
        return RequestOptions.builder()
                .setApiKey(stripeApiKey)
                .build();
    }
}
