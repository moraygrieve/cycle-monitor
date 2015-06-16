package com.jtech.ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import com.jtech.ui.model.StationUpdateEntry;
import com.jtech.ui.model.StationUpdateTable;
import com.jtech.ui.scenario.ScenarioService;

public class Main extends Application {
	private static final Logger logger = LogManager.getLogger("Application");

	public static void main(String[] args) {
		Application.launch(Main.class, (java.lang.String[])null);
	}

	@Override
	public void start(Stage stage) throws Exception {
		ScenarioService sService = new ScenarioService(15903);
		StationUpdateTable suTable = new StationUpdateTable("DV_StationUpdate", sService);

    	logger.info("Calling update connection");
		sService.updateConnection("localhost");
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application.fxml"));
			Parent root = (Parent) fxmlLoader.load();
			Controller controller = fxmlLoader.<Controller>getController();
			
			Scene scene = new Scene(root, 2000, 1200);
			stage.setTitle("Cycle Monitor");
			stage.setScene(scene);
			stage.show();

			new MapController(controller);
			new TableController(controller, suTable);
					       
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}