package com.jtech.ui.model;

import com.jtech.ui.scenario.ScenarioService;
import com.jtech.ui.scenario.ScenarioTable;

public class StationUpdateTable extends ScenarioTable<StationUpdateEntry> {

    public StationUpdateTable(String name, ScenarioService scenarioService) throws ClassNotFoundException {
        super(name, scenarioService, StationUpdateEntry.class);
    }
}