// Cycle Monitor, Copyright (C) 2016  M.B.Grieve
// This file is part of the Cycle Monitor example application.
//
// Cycle Monitor is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Cycle Monitor is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with City Monitor.  If not, see <http://www.gnu.org/licenses/>.
//
// Contact: moray.grieve@me.com

package com.jtech.ui.scenario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.apama.EngineException;
import com.apama.event.Event;
import com.apama.services.event.IEventService;
import com.apama.services.scenario.DiscoveryStatusEnum;
import com.apama.services.scenario.IScenarioService;
import com.apama.services.scenario.ScenarioServiceFactory;
import com.apama.services.scenario.internal.ScenarioDefinition;

public class ScenarioService {
    private static final Logger logger = LogManager.getLogger("ScenarioService");

    private final int port;
    private IScenarioService scenarioService;
    private Map<String, ScenarioDefinition> scenarios;
    private CopyOnWriteArrayList<IScenarioServiceSubscriber> subscribers = new CopyOnWriteArrayList<IScenarioServiceSubscriber>();

    public ScenarioService(int port) {
        this.port = port;
    }

    private void connect(final String host) {
        try {
            scenarioService = ScenarioServiceFactory.createScenarioService(host, port, null, null);

            scenarioService.addListener(IScenarioService.PROPERTY_SCENARIO_DISCOVERY_STATUS, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (scenarioService.getDiscoveryStatus().equals(DiscoveryStatusEnum.COMPLETE)) {
                        for (IScenarioServiceSubscriber subscriber : subscribers) {
                            logger.info("Notifying subscriber: " + subscriber.getId());
                            subscriber.onDiscoveryComplete();
                        }
                    }
                }
            });

            scenarioService.addListener(IScenarioService.PROPERTY_SCENARIO_ADDED, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    final ScenarioDefinition definition = (ScenarioDefinition) evt.getNewValue();
                    logger.info("Adding scenario to map: " + definition.getId());
                    scenarios.put(definition.getId(), definition);
                }
            });

            scenarioService.getEventService().addPropertyChangeListener(IEventService.PROPERTY_CONNECTED, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getNewValue().equals(true)) {
                        logger.info("Scenario service is connected");
                        for (IScenarioServiceSubscriber subscriber : subscribers) subscriber.onConnect();
                    } else {
                        logger.info("Scenario service is disconnected");
                        for (IScenarioServiceSubscriber subscriber : subscribers) subscriber.onDisconnect();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConnection(final String host) {
        scenarios = new HashMap<String, ScenarioDefinition>();
        if (scenarioService != null) scenarioService.dispose();
        for (IScenarioServiceSubscriber subscriber : subscribers) subscriber.onDisconnect();
        connect(host);
    }

    public void sendEvent(Event event) {
        try {
            if (scenarioService.getEventService().isConnected())
                scenarioService.getEventService().sendEvent(event);
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void register(IScenarioServiceSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public ScenarioDefinition getScenarioDefinition(String name) {
        if (scenarios.containsKey(name)) return scenarios.get(name);
        return null;
    }
}
