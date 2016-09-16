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

package com.jtech.ui.scenario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.apama.services.scenario.DiscoveryStatusEnum;
import com.apama.services.scenario.IScenarioInstance;
import com.apama.services.scenario.internal.ScenarioDefinition;
import com.apama.services.scenario.internal.ScenarioInstance;
import com.jtech.ui.annotation.Setter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ScenarioTable<T extends IScenarioEntry> extends IScenarioServiceSubscriber {
	private static final Logger logger = LogManager.getLogger("ScenarioTable");

	private final String name;
	private final Class dataClazz;
	private final ObservableList<T> dataCache;
	private final ScenarioService scenarioService;
	private final Map<String, Method> methodTable;

	protected ScenarioTable(String name, ScenarioService scenarioService, Class clazz) throws ClassNotFoundException {
		this.name = name;
		this.dataClazz = clazz;
		this.dataCache = FXCollections.observableArrayList();
		this.scenarioService = scenarioService;
		this.methodTable = new HashMap<String, Method>();
		scenarioService.register(this);
		createMethodTable();
	}

	public ObservableList<T> getDataCache() {
		return dataCache;
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public void onDisconnect() {
		Platform.runLater(new Runnable() {
			public void run() {
				dataCache.clear();
			}
		});
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void onDiscoveryComplete() {
		final ScenarioDefinition definition = scenarioService.getScenarioDefinition(name);
		if (definition == null) return;

		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_DISCOVERY_STATUS, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (definition.getDiscoveryStatus().equals(DiscoveryStatusEnum.COMPLETE)) {
					logger.info("Instance discovery is complete for " + getId());

					Platform.runLater(new Runnable() {
						public void run() {
							for (final IScenarioInstance instance : definition.getInstances()) {
								addInstance(instance);
							}
						}
					});

					onInstanceDiscoveryComplete(definition);
				}
			}
		});
	}

	private void onInstanceDiscoveryComplete(ScenarioDefinition definition) {
		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_ADDED, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				Platform.runLater(new Runnable() {
					public void run() {
						logger.info("Property change on " + getId() + " for PROPERTY_INSTANCE_ADDED");
						addInstance((ScenarioInstance) evt.getNewValue());
					}
				});
			}
		});

		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_UPDATED, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				Platform.runLater(new Runnable() {
					public void run() {
						logger.info("Property change on " + getId() + " for PROPERTY_INSTANCE_UPDATED");
						editInstance((ScenarioInstance) evt.getNewValue());
					}
				});
			}
		});

		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_REMOVED, new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				Platform.runLater(new Runnable() {
					public void run() {
						logger.info("Property change on " + getId() + " for PROPERTY_INSTANCE_REMOVED");
						removeInstance((ScenarioInstance) evt.getNewValue());
					}
				});
			}
		});
	}

	private void createMethodTable() {
		for (Method method : this.dataClazz.getMethods()) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof Setter) {
					logger.info("Setter for " + ((Setter) annotation).name() + " is " + method.getName());
					methodTable.put(((Setter) annotation).name(), method);
				}
			}
		}
	}

	private void addInstance(IScenarioInstance instance) {
		try {
			T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class)
					.newInstance(instance.getScenarioDefinition().getId(), instance.getId());

			for (String key : instance.getScenarioDefinition().getOutputParameterNames()) {
				if (methodTable.containsKey(key)) {
					try {
						methodTable.get(key).setAccessible(true);
						methodTable.get(key).invoke(data, instance.getValue(key));
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
			logger.info("Adding instance to data cache: " + data.getId());
			dataCache.add(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void editInstance(IScenarioInstance instance) {
		try {
			T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class)
					.newInstance(instance.getScenarioDefinition().getId(), instance.getId());

			int index = dataCache.indexOf(data);
			if (index != -1) {
				for (String key : instance.getScenarioDefinition().getOutputParameterNames()) {
					if (methodTable.containsKey(key)) {
						try {
							methodTable.get(key).setAccessible(true);
							methodTable.get(key).invoke(data, instance.getValue(key));
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
			logger.info("Updating instance in data cache: " + data.getId());
			dataCache.set(index, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeInstance(IScenarioInstance instance) {
		try {
			T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class)
					.newInstance(instance.getScenarioDefinition().getId(), instance.getId());

			if (dataCache.indexOf(data) != -1) {
				logger.info("Removing instance from data cache: " + data.getId());
				dataCache.remove(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
