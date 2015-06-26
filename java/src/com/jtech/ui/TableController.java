package com.jtech.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.paint.Color;

import com.jtech.ui.model.StationAlertEntry;
import com.jtech.ui.model.StationAlertTable;

public class TableController {

	public TableController(Controller controller, StationAlertTable stationAlertTable) {
		controller.idColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Long>("stationId"));
		controller.cityColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("city"));
		controller.typeColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("type"));
		controller.messageColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("message"));	  
		controller.stationAlertTable.setItems(stationAlertTable.getDataCache());    

		controller.typeColumn.setCellFactory(new Callback<TableColumn<StationAlertEntry, String>, TableCell<StationAlertEntry, String>>() {
			public TableCell<StationAlertEntry, String> call(TableColumn<StationAlertEntry, String> tradesEntryStringTableColumn) {
				final TableCell<StationAlertEntry, String> cell = new TableCell<StationAlertEntry, String>() {
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty()) {
							StationAlertEntry entry = (StationAlertEntry) this.getTableRow().getItem();
							setTextFill(Color.BLACK);
							setStyle("-fx-background-color: " + entry.getColor()+ "; -fx-opacity: 0.75; ");
							setText(item);
						} else {
							setText(null);
							setTextFill(null);
							setStyle(null);
							return;
						}
					}
				};
				return cell;
			}
		});
	}	
}
