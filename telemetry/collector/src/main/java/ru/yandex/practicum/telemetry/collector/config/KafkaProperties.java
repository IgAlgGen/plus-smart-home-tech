package ru.yandex.practicum.telemetry.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";
    private String sensorTopic = "telemetry.sensors.v1";
    private String hubTopic = "telemetry.hubs.v1";
}
