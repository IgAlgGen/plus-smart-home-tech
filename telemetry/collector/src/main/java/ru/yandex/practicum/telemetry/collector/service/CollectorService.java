package ru.yandex.practicum.telemetry.collector.service;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.KafkaProperties;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.mapper.EventAvroMapper;

@Service
public class CollectorService {

    private final Producer<String, byte[]> producer;
    private final KafkaProperties kafkaProperties;
    private final EventAvroMapper eventAvroMapper;
    private final AvroBinarySerializer avroBinarySerializer;

    public CollectorService(Producer<String, byte[]> producer,
                            KafkaProperties kafkaProperties,
                            EventAvroMapper eventAvroMapper,
                            AvroBinarySerializer avroBinarySerializer) {
        this.producer = producer;
        this.kafkaProperties = kafkaProperties;
        this.eventAvroMapper = eventAvroMapper;
        this.avroBinarySerializer = avroBinarySerializer;
    }

    public void collectSensorEvent(SensorEvent event) {
        byte[] payload = avroBinarySerializer.serialize(eventAvroMapper.toAvro(event));
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                kafkaProperties.getSensorTopic(),
                event.getHubId(),
                payload
        );
        producer.send(record);
    }

    public void collectHubEvent(HubEvent event) {
        byte[] payload = avroBinarySerializer.serialize(eventAvroMapper.toAvro(event));
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                kafkaProperties.getHubTopic(),
                event.getHubId(),
                payload
        );
        producer.send(record);
    }
}
