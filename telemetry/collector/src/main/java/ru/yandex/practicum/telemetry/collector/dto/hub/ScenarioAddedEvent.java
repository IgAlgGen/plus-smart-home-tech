package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScenarioCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<ScenarioCondition> conditions) {
        this.conditions = conditions;
    }

    public List<DeviceAction> getActions() {
        return actions;
    }

    public void setActions(List<DeviceAction> actions) {
        this.actions = actions;
    }
}
