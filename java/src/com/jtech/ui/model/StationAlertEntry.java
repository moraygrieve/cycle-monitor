package com.jtech.ui.model;

import com.jtech.ui.annotation.Setter;
import com.jtech.ui.scenario.IScenarioEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;

public class StationAlertEntry extends IScenarioEntry {
	SimpleStringProperty city = new SimpleStringProperty();
	SimpleLongProperty stationId = new SimpleLongProperty();
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

	public String getType() { return type.get(); }
	public SimpleStringProperty typeProperty() { return type; }
	@Setter(name="type", type="string") public void setType(String type) {
		this.type.set(type);
	}

	public String getMessage() { return message.get(); }
	public SimpleStringProperty messageProperty() { return message; }
	@Setter(name="message", type="string") public void setMessage(String message) {
		this.type.set(message);
	}

	public String getColor() { return color.get(); }
	public SimpleStringProperty colorProperty() { return color; }
	@Setter(name="color", type="string") public void setColor(String color) {
		this.color.set(color);
	}
}
