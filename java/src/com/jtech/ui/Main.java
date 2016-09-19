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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.scenario.ScenarioService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private static final Logger logger = LogManager.getLogger("Application");

	public static void main(String[] args) {
		Application.launch(Main.class, (java.lang.String[])null);
	}

	@Override
	public void start(Stage stage) throws Exception {
		ScenarioService sService = new ScenarioService(15903);
		StationAlertTable stationUpdateTable = new StationAlertTable("DV_StationAlert", sService);
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			Controller controller = fxmlLoader.<Controller>getController();
			
			Scene scene = new Scene(root, 1200, 600);
			stage.setTitle("Cycle Monitor");
			stage.setScene(scene);
			stage.show();

			MapController mapController = new MapController(controller, "DV_StationAlert", sService, stationUpdateTable);
			new TableController(controller, mapController, stationUpdateTable);
	
			logger.info("Calling update connection");
			sService.updateConnection("localhost");
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}