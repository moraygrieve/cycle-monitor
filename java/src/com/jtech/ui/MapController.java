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

package com.jtech.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.apama.services.scenario.DiscoveryStatusEnum;
import com.apama.services.scenario.IScenarioInstance;
import com.apama.services.scenario.internal.ScenarioDefinition;
import com.jtech.ui.model.StationAlertEntry;
import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.scenario.IScenarioServiceSubscriber;
import com.jtech.ui.scenario.ScenarioService;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class MapController extends IScenarioServiceSubscriber {	
    private static final Logger logger = LogManager.getLogger("MapController");

    private final String scenarioName;
    private final ScenarioService scenarioService;
    private final StationAlertTable stationAlertTable;
	private final WebEngine webEngine;
	private AtomicBoolean instancesLoaded = new AtomicBoolean(false);
	private AtomicBoolean mapLoaded = new AtomicBoolean(false);
	private AtomicBoolean loaded = new AtomicBoolean(false);
	
	private final Map<Long, StationAlertEntry> stations = new HashMap<Long, StationAlertEntry>();

	public MapController(Controller controller, String scenarioName, ScenarioService scenarioService, StationAlertTable stationAlertTable) {
		this.scenarioName = scenarioName;
		this.scenarioService = scenarioService;
		this.stationAlertTable = stationAlertTable;
		scenarioService.register(this);
		
		final URL urlGoogleMaps = getClass().getClassLoader().getResource("googlemap.html");
		webEngine = controller.webViewPanel.getEngine();

		controller.webViewPanel.setOnDragDetected(new EventHandler<MouseEvent>() {
	            //workaround for google maps API bug
				public void handle(MouseEvent t) {}
	        });
		
		webEngine.load(urlGoogleMaps.toExternalForm());
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<Worker.State>() {
					public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
						if (newState == Worker.State.SUCCEEDED) {
							mapLoaded.set(true);
							logger.info("Map is loaded");
							if (instancesLoaded.get()) drawInitial();
						}
					}
				});


		stationAlertTable.getDataCache().addListener(new ListChangeListener<StationAlertEntry>(){
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends StationAlertEntry> c) {
				while (loaded.get() && c.next()) {
					if (c.wasPermutated()) { } 
					else if (c.wasUpdated()) { } 
					else {
						for (StationAlertEntry remitem : c.getRemoved()) {
							if (stations.containsKey(remitem.getId())) {
								logger.info("Removing station " + remitem.getId() + " from map");
								stations.remove(remitem.getId());
								deleteStation(remitem);
							}
						}
						for (StationAlertEntry additem : c.getAddedSubList()) {
							if (!stations.containsKey(additem.getId())) {
								logger.info("Adding station " + additem.getId() + " to map");
								stations.put(additem.getId(), additem);
								drawStation(additem);
							}
						}
					}
				}
			}
		});
	}
	
	@Override
	public String getId() {
		return scenarioName;
	}

	@Override
	public void onDisconnect() {
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void onDiscoveryComplete() {
		final ScenarioDefinition definition = scenarioService.getScenarioDefinition(scenarioName);
		if (definition == null) return;

		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_DISCOVERY_STATUS, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (definition.getDiscoveryStatus().equals(DiscoveryStatusEnum.COMPLETE)) {
					logger.info("Scenario instances are loaded");
					instancesLoaded.set(true);
					if (mapLoaded.get()) drawInitial();
				}
			}
		});
	}
	
	private void drawInitial() {
		int index=0;
		while (index<stationAlertTable.getDataCache().size()) {
			StationAlertEntry entry = stationAlertTable.getDataCache().get(index);
			logger.info("Adding station " + entry.getId() + " to map");
			stations.put(entry.getId(), entry);
			drawStation(stationAlertTable.getDataCache().get(index));
			index++;
		}
		loaded.set(true);
	}
	
	public void showDocumentWindow(StationAlertEntry entry) {
		webEngine.executeScript("document.showDocumentWindow('"+getStationTitle(entry)+"',"+
				entry.getStationLat() + "," + entry.getStationLng()+")");
	}
	
	private void drawStation(StationAlertEntry entry) {
		webEngine.executeScript("document.drawStation(" + entry.getId() + "," + entry.getStationLat() + "," + entry.getStationLng() + 
				",'"+entry.getColor()+"','"+getStationTitle(entry)+"',6.0)");
	}

	private void deleteStation(StationAlertEntry entry) {
		webEngine.executeScript("document.deleteStation(" + entry.getId()+")");
	}

	private void setMapCentre(double lat, double lng) {
		webEngine.executeScript("document.setCentre(" + lat + "," + lng + ")");
	}

	private void setZoom(int level) {
		webEngine.executeScript("document.setZoom(" + level + ")");
	}

	private String getStationTitle(StationAlertEntry entry) {
		String name = entry.getStationName().replace(",", "&#44").replace("'", "").replace("(", "&#40").replace(")", "&#41");
		String title = "<br><b>Name:</b> " + name + "</br>" +
				"<br><b>ID:</b> " + entry.getStationId() + "</br>" +
				"<br><b>Total:</b> " + (entry.getNumEmpty() + entry.getNumDocked()) + "</br>" +
				"<br><b>Emtpy:</b> " + entry.getNumEmpty() + "</br>" +
				"<br><b>Alert:</b> " + entry.getType() + "</br>";
		return title;
	}
}
