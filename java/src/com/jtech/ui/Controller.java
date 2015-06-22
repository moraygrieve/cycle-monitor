package com.jtech.ui;

import com.jtech.ui.model.StationAlertEntry;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;

public class Controller {
    @FXML WebView webViewPanel;
    @FXML TableView<StationAlertEntry> stationAlertTable;
    @FXML TableColumn<StationAlertEntry, Long> idColumn;
    @FXML TableColumn<StationAlertEntry, String> cityColumn;
    @FXML TableColumn<StationAlertEntry, String> typeColumn;
    @FXML TableColumn<StationAlertEntry, String> messageColumn;
    
    @FXML
    void initialize() {
    }
}
