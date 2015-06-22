package com.jtech.ui;

import javafx.scene.control.cell.PropertyValueFactory;

import com.jtech.ui.model.StationAlertTable;
import com.jtech.ui.model.StationUpdateEntry;
import com.jtech.ui.model.StationUpdateTable;

public class TableController {

	public TableController(Controller controller, StationUpdateTable stationUpdateTable,
			StationAlertTable stationAlertTable) {
    	controller.idColumn.setCellValueFactory(new PropertyValueFactory<StationUpdateEntry, Long>("stationId"));
    	controller.nameColumn.setCellValueFactory(new PropertyValueFactory<StationUpdateEntry, String>("stationName"));
    	controller.dockedColumn.setCellValueFactory(new PropertyValueFactory<StationUpdateEntry, Long>("numDocked"));
    	controller.emptyColumn.setCellValueFactory(new PropertyValueFactory<StationUpdateEntry, Long>("numEmpty"));	  
    	controller.stationUpdateTable.setItems(stationUpdateTable.getDataCache());        	
	}	
}
