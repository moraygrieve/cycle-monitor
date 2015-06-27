// Cycle Monitor, Copyright (C) 2015  M.B.Grieve
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

import com.jtech.ui.model.StationAlertEntry;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.web.WebView;

public class Controller {
    @FXML WebView webViewPanel;
    @FXML TableView<StationAlertEntry> stationAlertTable;
    @FXML TableColumn<StationAlertEntry, Long> idColumn;
    @FXML TableColumn<StationAlertEntry, Double> timestampColumn;
    @FXML TableColumn<StationAlertEntry, String> typeColumn;
    @FXML TableColumn<StationAlertEntry, String> messageColumn;
    
    @FXML
    void initialize() {
    }
}
