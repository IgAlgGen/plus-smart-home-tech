package ru.yandex.practicum.telemetry.collector.service.hub;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.yandex.practicum.telemetry.collector.config.KafkaProperties;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.mapper.EventAvroMapper;
import ru.yandex.practicum.telemetry.collector.service.AvroBinarySerializer;

public abstract class BaseHubEventHandler implements HubEventHandler {

    private final Producer<String, byte[]> producer;
    private final KafkaProperties kafkaProperties;
    private final EventAvroMapper eventAvroMapper;
    private final AvroBinarySerializer avroBinarySerializer;

    protected BaseHubEventHandler(Producer<String, byte[]> producer,
                                  KafkaProperties kafkaProperties,
                                  EventAvroMapper eventAvroMapper,
                                  AvroBinarySerializer avroBinarySerializer) {
        this.producer = producer;
        this.kafkaProperties = kafkaProperties;
        this.eventAvroMapper = eventAvroMapper;
        this.avroBinarySerializer = avroBinarySerializer;
    }

    @Override
    public void handle(HubEvent event) {
        if (!event.getType().equals(getMessageType())) {
            throw new IllegalArgumentException("Unsupported hub event type: " + event.getType());
        }

        byte[] payload = avroBinarySerializer.serialize(eventAvroMapper.toAvro(event));
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                kafkaProperties.getHubTopic(),
                event.getHubId(),
                payload
        );
        producer.send(record);
    }
}
