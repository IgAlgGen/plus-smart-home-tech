package ru.yandex.practicum.telemetry.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.dto.hub.*;
import ru.yandex.practicum.telemetry.collector.dto.sensor.*;

import java.util.List;

@Component
public class EventAvroMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        SensorEventAvro sensorEventAvro = new SensorEventAvro();
        sensorEventAvro.setId(event.getId());
        sensorEventAvro.setHubId(event.getHubId());
        sensorEventAvro.setTimestamp(event.getTimestamp());
        sensorEventAvro.setPayload(toSensorPayload(event));
        return sensorEventAvro;
    }

    public HubEventAvro toAvro(HubEvent event) {
        HubEventAvro hubEventAvro = new HubEventAvro();
        hubEventAvro.setHubId(event.getHubId());
        hubEventAvro.setTimestamp(event.getTimestamp());
        hubEventAvro.setPayload(toHubPayload(event));
        return hubEventAvro;
    }

    private Object toSensorPayload(SensorEvent event) {
        if (event instanceof ClimateSensorEvent climate) {
            ClimateSensorAvro avro = new ClimateSensorAvro();
            avro.setTemperatureC(climate.getTemperatureC());
            avro.setHumidity(climate.getHumidity());
            avro.setCo2Level(climate.getCo2Level());
            return avro;
        }
        if (event instanceof LightSensorEvent light) {
            LightSensorAvro avro = new LightSensorAvro();
            avro.setLinkQuality(light.getLinkQuality());
            avro.setLuminosity(light.getLuminosity());
            return avro;
        }
        if (event instanceof MotionSensorEvent motion) {
            MotionSensorAvro avro = new MotionSensorAvro();
            avro.setLinkQuality(motion.getLinkQuality());
            avro.setMotion(motion.getMotion());
            avro.setVoltage(motion.getVoltage());
            return avro;
        }
        if (event instanceof SwitchSensorEvent sensorEvent) {
            SwitchSensorAvro avro = new SwitchSensorAvro();
            avro.setState(sensorEvent.getState());
            return avro;
        }
        if (event instanceof TemperatureSensorEvent temperature) {
            TemperatureSensorAvro avro = new TemperatureSensorAvro();
            avro.setId(temperature.getId());
            avro.setHubId(temperature.getHubId());
            avro.setTimestamp(temperature.getTimestamp());
            avro.setTemperatureC(temperature.getTemperatureC());
            avro.setTemperatureF(temperature.getTemperatureF());
            return avro;
        }
        throw new IllegalArgumentException("Unsupported sensor event type: " + event.getType());
    }

    private Object toHubPayload(HubEvent event) {
        if (event instanceof DeviceAddedEvent deviceAdded) {
            DeviceAddedEventAvro avro = new DeviceAddedEventAvro();
            avro.setId(deviceAdded.getId());
            avro.setType(DeviceTypeAvro.valueOf(deviceAdded.getDeviceType().name()));
            return avro;
        }
        if (event instanceof DeviceRemovedEvent deviceRemoved) {
            DeviceRemovedEventAvro avro = new DeviceRemovedEventAvro();
            avro.setId(deviceRemoved.getId());
            return avro;
        }
        if (event instanceof ScenarioAddedEvent scenarioAdded) {
            ScenarioAddedEventAvro avro = new ScenarioAddedEventAvro();
            avro.setName(scenarioAdded.getName());
            avro.setConditions(toConditions(scenarioAdded.getConditions()));
            avro.setActions(toActions(scenarioAdded.getActions()));
            return avro;
        }
        if (event instanceof ScenarioRemovedEvent scenarioRemoved) {
            ScenarioRemovedEventAvro avro = new ScenarioRemovedEventAvro();
            avro.setName(scenarioRemoved.getName());
            return avro;
        }
        throw new IllegalArgumentException("Unsupported hub event type: " + event.getType());
    }

    private List<ScenarioConditionAvro> toConditions(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(this::toCondition)
                .toList();
    }

    private ScenarioConditionAvro toCondition(ScenarioCondition condition) {
        ScenarioConditionAvro avro = new ScenarioConditionAvro();
        avro.setSensorId(condition.getSensorId());
        avro.setType(ConditionTypeAvro.valueOf(condition.getType().name()));
        avro.setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));

        Object value = condition.getValue();
        if (value == null || value instanceof Integer || value instanceof Boolean) {
            avro.setValue(value);
            return avro;
        }

        throw new IllegalArgumentException("Scenario condition value supports only null, integer or boolean");
    }

    private List<DeviceActionAvro> toActions(List<DeviceAction> actions) {
        return actions.stream()
                .map(this::toAction)
                .toList();
    }

    private DeviceActionAvro toAction(DeviceAction action) {
        DeviceActionAvro avro = new DeviceActionAvro();
        avro.setSensorId(action.getSensorId());
        avro.setType(ActionTypeAvro.valueOf(action.getType().name()));
        avro.setValue(action.getValue());
        return avro;
    }
}
