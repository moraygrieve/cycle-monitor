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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jtech.ui.model.StationAlertEntry;
import com.jtech.ui.model.StationAlertTable;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class TableController {
    private static final Logger logger = LogManager.getLogger("TableController");

	public TableController(final Controller controller, final MapController mapController, 
			StationAlertTable stationAlertTable) {
		controller.idColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Long>("stationId"));
		controller.typeColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("type"));
		controller.messageColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, String>("message"));	  
		controller.timestampColumn.setCellValueFactory(new PropertyValueFactory<StationAlertEntry, Double>("timestamp"));

		controller.stationAlertTable.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2) {
					StationAlertEntry entry = controller.stationAlertTable.getSelectionModel().getSelectedItem();
					logger.info("Selected entry id " + entry.getId());
					mapController.showDocumentWindow(entry);
				}
			}
		});

		controller.typeColumn.setCellFactory(new Callback<TableColumn<StationAlertEntry, String>, TableCell<StationAlertEntry, String>>() {
			public TableCell<StationAlertEntry, String> call(TableColumn<StationAlertEntry, String> tradesEntryStringTableColumn) {
				final TableCell<StationAlertEntry, String> cell = new TableCell<StationAlertEntry, String>() {
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);

						if (item == null) {
							setText("");
							return;
						}
						
						StationAlertEntry entry = (StationAlertEntry) this.getTableRow().getItem();
						if (entry == null) {
							setText(null);
							return;
						}
						setTextFill(Color.BLACK);
						setStyle("-fx-background-color: " + entry.getColor()+ "; -fx-opacity: 0.75; ");
						setText(item);	
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
						
						if (item == null) {
							setText("");
							return;
						}
						String tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((long) (item*1000L))); 
						setText(tstamp);
					}
				};
				return cell;
			}
		});
		
		controller.messageColumn.setCellFactory(new Callback<TableColumn<StationAlertEntry, String>, TableCell<StationAlertEntry, String>>() {
			public TableCell<StationAlertEntry, String> call(TableColumn<StationAlertEntry, String> tradesEntryStringTableColumn) {
				final TableCell<StationAlertEntry, String> cell = new TableCell<StationAlertEntry, String>() {
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);

						if (item == null) {
							setText("");
							return;
						}
						setTooltip(new Tooltip(item));
						setText(item);
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

		controller.stationAlertTable.setItems(stationAlertTable.getDataCache());    
	}	
}
