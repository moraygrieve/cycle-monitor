package com.jtech.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.model.StationAlertEntry;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class MapController {
	private final WebEngine webEngine;
	private volatile boolean loaded;
	private final Map<Long, StationAlertEntry> stations = new HashMap<Long, StationAlertEntry>();

	public MapController(Controller controller, final StationAlertTable stationAlertTable) {
		final URL urlGoogleMaps = getClass().getClassLoader().getResource("googlemap.html");
		webEngine = controller.webViewPanel.getEngine();

		webEngine.load(urlGoogleMaps.toExternalForm());
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<Worker.State>() {
					public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
						if (newState == Worker.State.SUCCEEDED) {
							JSObject window = (JSObject) webEngine.executeScript("window");

							int index=0;
							while (index<stationAlertTable.getDataCache().size()) {
								stations.put(stationAlertTable.getDataCache().get(index).getId(), stationAlertTable.getDataCache().get(index));
								drawStation(stationAlertTable.getDataCache().get(index));
								index++;
							}
							loaded=true;
						}
					}
				});


		stationAlertTable.getDataCache().addListener(new ListChangeListener<StationAlertEntry>(){
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends StationAlertEntry> c) {
				while (loaded && c.next()) {
					for (StationAlertEntry additem : c.getAddedSubList()) {
						if (!stations.containsKey(additem.getId())) {
							stations.put(additem.getId(), additem);
							drawStation(additem);
						}
					}
				}
			}
		});
	}

	private void drawStation(StationAlertEntry entry) {
		webEngine.executeScript("document.drawStation(" + entry.getId() + "," + entry.getStationLat() + "," + entry.getStationLng() + 
				",'"+entry.getColor()+"','"+getStationTitle(entry)+"',6.0)");
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
