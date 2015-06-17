package com.jtech.ui;

import java.net.URL;

import com.jtech.ui.model.StationUpdateEntry;
import com.jtech.ui.model.StationUpdateTable;

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

	public MapController(Controller controller, StationUpdateTable suTable) {
		final URL urlGoogleMaps = getClass().getClassLoader().getResource("googlemap.html");
		webEngine = controller.webViewPanel.getEngine();

		webEngine.load(urlGoogleMaps.toExternalForm());
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<Worker.State>() {
					public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
						if (newState == Worker.State.SUCCEEDED) {
							JSObject window = (JSObject) webEngine.executeScript("window");
						}
					}
				});


		suTable.getDataCache().addListener(new ListChangeListener<StationUpdateEntry>(){
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends StationUpdateEntry> pChange) {
				while(pChange.next()) {
					if (pChange.wasPermutated()) {

					}
					else if (pChange.wasUpdated()) {

					} 
					else {
						for (StationUpdateEntry remitem : pChange.getRemoved()) {

						}
						for (StationUpdateEntry additem : pChange.getAddedSubList()) {
							System.out.println(("Added station " + additem.getId()));
							//drawStation(additem);
						}
					}
				}
			}
		});
	}

	public void drawStation(StationUpdateEntry entry) {
		webEngine.executeScript("document.drawStation(" + entry.getId() + "," + entry.getStationLat() + "," + entry.getStationLng() + 
				",'black','title',2)");
	}

	public void setMapCentre(double lat, double lng) {
		webEngine.executeScript("document.setCentre(" + lat + "," + lng + ")");
	}

	public void setZoom(int level) {
		webEngine.executeScript("document.setZoom(" + level + ")");
	}

    private String getStationTitle(StationUpdateEntry entry) {
        String name = entry.getName().replace(",", "&#44").replace("'", "").replace("(", "&#40").replace(")", "&#41");
        String title = "<br><b>Name:</b> " + name + "</br>" +
                "<br><b>ID:</b> " + entry.getId() + "</br>" +
                "<br><b>Total:</b> " + (entry.getNumEmpty() + entry.getNumDocked()) + "</br>" +
                "<br><b>Emtpy:</b> " + entry.getNumEmpty() + "</br>";
        return title;
    }
}
