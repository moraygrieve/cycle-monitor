package com.jtech.ui;

import javafx.scene.control.cell.PropertyValueFactory;

import com.jtech.ui.model.StationAlertEntry;
import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.model.StationUpdateEntry;
import com.jtech.ui.model.StationUpdateTable;

public class TableController {

	public TableController(Controller controller, StationUpdateTable stationUpdateTable,
			StationAlertTable stationAlertTable) {
    	controller.idColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Long>("stationId"));
    	controller.cityColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("city"));
    	controller.typeColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("type"));
    	controller.messageColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("message"));	  
    	controller.stationAlertTable.setItems(stationAlertTable.getDataCache());        	
	}	
}
