package com.tcon.financial_service.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name("payment-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("payment-failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundCompletedTopic() {
        return TopicBuilder.name("refund-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic payoutCompletedTopic() {
        return TopicBuilder.name("payout-completed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

