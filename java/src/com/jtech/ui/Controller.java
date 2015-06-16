package com.jtech.ui;

import com.jtech.ui.model.StationUpdateEntry;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller {
    @FXML TableView<StationUpdateEntry> stationUpdateTable;
    @FXML TableColumn<StationUpdateEntry, Long> idColumn;
    @FXML TableColumn<StationUpdateEntry, String> nameColumn;
    @FXML TableColumn<StationUpdateEntry, Double> latColumn;
    @FXML TableColumn<StationUpdateEntry, Double> lngColumn;
    @FXML TableColumn<StationUpdateEntry, Long> dockedColumn;
    @FXML TableColumn<StationUpdateEntry, Long> emptyColumn;
    
    @FXML
    void initialize() {
    }
}
