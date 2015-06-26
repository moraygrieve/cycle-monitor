package com.jtech.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.scenario.ScenarioService;

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

			new MapController(controller, stationUpdateTable);
			new TableController(controller, stationUpdateTable);
	
			logger.info("Calling update connection");
			sService.updateConnection("localhost");
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}