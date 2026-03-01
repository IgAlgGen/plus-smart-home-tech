package ru.yandex.practicum.telemetry.collector.service.hub;

import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.KafkaProperties;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.mapper.EventAvroMapper;
import ru.yandex.practicum.telemetry.collector.service.AvroBinarySerializer;

@Service
public class DeviceAddedEventHandler extends BaseHubEventHandler {
    public DeviceAddedEventHandler(Producer<String, byte[]> producer,
                                   KafkaProperties kafkaProperties,
                                   EventAvroMapper eventAvroMapper,
                                   AvroBinarySerializer avroBinarySerializer) {
        super(producer, kafkaProperties, eventAvroMapper, avroBinarySerializer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }
}
