package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScenarioAddedEvent extends HubEvent {

    @NotBlank
    private String name;
    @NotEmpty
    @Valid
    private List<ScenarioCondition> conditions;
    @NotEmpty
    @Valid
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
