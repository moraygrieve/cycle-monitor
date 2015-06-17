package com.jtech.ui.scenario;

import com.apama.services.scenario.IScenarioInstance;
import com.apama.services.scenario.internal.ScenarioDefinition;
import com.apama.services.scenario.internal.ScenarioInstance;
import com.jtech.ui.annotation.Setter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ScenarioTable<T extends IScenarioEntry> extends IScenarioServiceSubscriber {
    private static final Logger logger = LogManager.getLogger("ScenarioTable");

    private final String name;
    private final Class dataClazz;
    private final ObservableList<T> dataCache;
    private final ScenarioService scenarioService;
    private final Map<String, Method> methodTable;

    protected ScenarioTable(String name, ScenarioService scenarioService, Class clazz)
            throws ClassNotFoundException {
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

        definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_ADDED, new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        addInstance((ScenarioInstance) evt.getNewValue());
                    }
                });
            }
        });

        definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_UPDATED, new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        editInstance((ScenarioInstance) evt.getNewValue());
                    }
                });
            }
        });

        definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_REMOVED, new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        removeInstance((ScenarioInstance) evt.getNewValue());
                    }
                });
            }
        });

        for (final IScenarioInstance instance : definition.getInstances()) {
            Platform.runLater(new Runnable() {
                public void run() {
                    addInstance(instance);
                }
            });
        }
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
            T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class).
                    newInstance(instance.getScenarioDefinition().getId(), instance.getId());

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
            logger.debug("Adding instance to data cache: " + data);
            dataCache.add(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editInstance(IScenarioInstance instance) {
        try {
            T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class).
                    newInstance(instance.getScenarioDefinition().getId(), instance.getId());

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
            logger.debug("Updating instance in data cache: " + data);
            dataCache.set(index, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeInstance(IScenarioInstance instance) {
        try {
            T data = (T) this.dataClazz.getDeclaredConstructor(String.class, Long.class).
                    newInstance(instance.getScenarioDefinition().getId(), instance.getId());

            if (dataCache.indexOf(data) != -1) {
                logger.debug("Removing instance from data cache: " + data);
                dataCache.remove(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
