package com.jtech.ui.scenario;

public abstract class IScenarioServiceSubscriber {
    public abstract String getId();
    public abstract void onDisconnect();
    public abstract void onConnect();
    public abstract void onDiscoveryComplete();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IScenarioServiceSubscriber that = (IScenarioServiceSubscriber) o;
        if (!getId().equals(that.getId())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
