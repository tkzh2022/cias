package com.company.cs.infra.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final boolean enabled;
    private final String topic;

    public KafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.enabled:false}") boolean enabled,
            @Value("${app.kafka.topic:cs-chat-events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.enabled = enabled;
        this.topic = topic;
    }

    public void publishChatEvent(String payload) {
        if (!enabled) {
            log.info("Kafka disabled, skip publishing: {}", payload);
            return;
        }
        kafkaTemplate.send(topic, payload);
    }
}
