package ru.yandex.practicum.telemetry.collector.service.sensor;

import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.KafkaProperties;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEventType;
import ru.yandex.practicum.telemetry.collector.mapper.EventAvroMapper;
import ru.yandex.practicum.telemetry.collector.service.AvroBinarySerializer;

@Service
public class ClimateSensorEventHandler extends BaseSensorEventHandler {
    public ClimateSensorEventHandler(Producer<String, byte[]> producer,
                                     KafkaProperties kafkaProperties,
                                     EventAvroMapper eventAvroMapper,
                                     AvroBinarySerializer avroBinarySerializer) {
        super(producer, kafkaProperties, eventAvroMapper, avroBinarySerializer);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
