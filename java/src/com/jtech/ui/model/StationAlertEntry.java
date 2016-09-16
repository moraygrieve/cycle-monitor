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

package com.jtech.ui.model;

import com.jtech.ui.annotation.Setter;
import com.jtech.ui.scenario.IScenarioEntry;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class StationAlertEntry extends IScenarioEntry {
	SimpleStringProperty city = new SimpleStringProperty();
	SimpleLongProperty   stationId = new SimpleLongProperty();
	SimpleStringProperty stationName = new SimpleStringProperty();
	SimpleDoubleProperty stationLat = new SimpleDoubleProperty();
	SimpleDoubleProperty stationLng = new SimpleDoubleProperty();
	SimpleLongProperty   numDocked = new SimpleLongProperty();
	SimpleLongProperty   numEmpty = new SimpleLongProperty();
	SimpleDoubleProperty timestamp = new SimpleDoubleProperty();
	SimpleStringProperty type = new SimpleStringProperty();
	SimpleStringProperty message = new SimpleStringProperty();
	SimpleStringProperty color = new SimpleStringProperty();

	public StationAlertEntry(String name, Long id) {
		super(name, id);
	}

	public String getCity() { return city.get(); }
	public SimpleStringProperty cityProperty() { return city; }
	@Setter(name="city", type="string") public void setCity(String city) {
		this.city.set(city);
	}

	public long getStationId() { return stationId.get(); }
	public SimpleLongProperty stationIdProperty() { return stationId; }
	@Setter(name="id", type="integer") public void setStationId(long id) {
		this.stationId.set(id);
	}

	public String getStationName() { return stationName.get(); }
	public SimpleStringProperty stationNameProperty() { return stationName; }
	@Setter(name="name", type="string") public void setName(String name) {
		this.stationName.set(name);
	}

	public double getStationLat() { return stationLat.get(); }
	public SimpleDoubleProperty stationLatProperty() { return stationLat; }
	@Setter(name="lat", type="float") public void setStationLat(double lat) {
		this.stationLat.set(lat);
	}

	public double getStationLng() { return stationLng.get(); }
	public SimpleDoubleProperty stationLngProperty() { return stationLng; }
	@Setter(name="lng", type="float") public void setStationLng(double lng) {
		this.stationLng.set(lng);
	}

	public long getNumDocked() { return numDocked.get(); }
	public SimpleLongProperty stationNumDocked() { return numDocked; }
	@Setter(name="docked", type="integer") public void setNumDocked(long docked) {
		this.numDocked.set(docked);
	}

	public long getNumEmpty() { return numEmpty.get(); }
	public SimpleLongProperty stationNumEmpty() { return numEmpty; }
	@Setter(name="empty", type="integer") public void setNumEmpty(long empty) {
		this.numEmpty.set(empty);
	}
	
	public double getTimestamp() { return timestamp.get(); }
	public SimpleDoubleProperty typeTimestamp() { return timestamp; }
	@Setter(name="timestamp", type="string") public void setTimestamp(double timestamp) {
		this.timestamp.set(timestamp);
	}
	
	public String getType() { return type.get(); }
	public SimpleStringProperty typeProperty() { return type; }
	@Setter(name="type", type="string") public void setType(String type) {
		this.type.set(type);
	}

	public String getMessage() { return message.get(); }
	public SimpleStringProperty messageProperty() { return message; }
	@Setter(name="message", type="string") public void setMessage(String message) {
		this.message.set(message);
	}

	public String getColor() { return color.get(); }
	public SimpleStringProperty colorProperty() { return color; }
	@Setter(name="color", type="string") public void setColor(String color) {
		this.color.set(color);
	}
}
