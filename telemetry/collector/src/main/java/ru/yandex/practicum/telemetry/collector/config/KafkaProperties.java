package ru.yandex.practicum.telemetry.collector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";
    private String sensorTopic = "telemetry.sensors.v1";
    private String hubTopic = "telemetry.hubs.v1";

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSensorTopic() {
        return sensorTopic;
    }

    public void setSensorTopic(String sensorTopic) {
        this.sensorTopic = sensorTopic;
    }

    public String getHubTopic() {
        return hubTopic;
    }

    public void setHubTopic(String hubTopic) {
        this.hubTopic = hubTopic;
    }
}
