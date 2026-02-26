package ru.yandex.practicum.telemetry.collector.dto.sensor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LightSensorEvent extends SensorEvent {

    private Integer linkQuality;
    private Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}
