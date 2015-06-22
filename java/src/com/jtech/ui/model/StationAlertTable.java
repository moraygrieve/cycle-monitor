package com.jtech.ui.model;

import com.jtech.ui.scenario.ScenarioService;
import com.jtech.ui.scenario.ScenarioTable;

public class StationAlertTable extends ScenarioTable<StationAlertEntry> {

    public StationAlertTable(String name, ScenarioService scenarioService) throws ClassNotFoundException {
        super(name, scenarioService, StationAlertTable.class);
    }
}