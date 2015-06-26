package com.jtech.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import com.jtech.ui.model.StationAlertEntry;
import com.jtech.ui.model.StationAlertTable;

public class TableController {

	public TableController(final Controller controller, StationAlertTable stationAlertTable) {
		controller.idColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Long>("stationId"));
		controller.typeColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("type"));
		controller.messageColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("message"));	  
		controller.timestampColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Double>("timestamp"));
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
		
		controller.timestampColumn.setCellFactory(new Callback<TableColumn<StationAlertEntry, Double>, TableCell<StationAlertEntry, Double>>() {
			public TableCell<StationAlertEntry, Double> call(TableColumn<StationAlertEntry, Double> tradesEntryStringTableColumn) {
				final TableCell<StationAlertEntry, Double> cell = new TableCell<StationAlertEntry, Double>() {
					protected void updateItem(Double item, boolean empty) {
						super.updateItem(item, empty);
						if (!isEmpty()) {
							String tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((long) (item*1000L))); 
							setText(tstamp);
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
		
		stationAlertTable.getDataCache().addListener(new ListChangeListener<StationAlertEntry>() {
            public void onChanged(Change<? extends StationAlertEntry> paramChange) {
                while (paramChange.next()) {
                    if (paramChange.wasAdded() && paramChange.getAddedSize() > 0) {
                        Platform.runLater(new Runnable() {
                            public void run() {
                                controller.stationAlertTable.getSortOrder().clear();
                                controller.stationAlertTable.getSortOrder().add(controller.timestampColumn);
                                controller.timestampColumn.setSortType(TableColumn.SortType.DESCENDING);
                                controller.timestampColumn.setSortable(true);
                            }
                        });
                    }
                }
            }
        });
	}	
}
