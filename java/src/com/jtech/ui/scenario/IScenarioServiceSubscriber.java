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
