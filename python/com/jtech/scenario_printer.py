# Cycle Monitor, Copyright (C) 2015  M.B.Grieve
# This file is part of the Cycle Monitor example application.
#
# Cycle Monitor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# Cycle Monitor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with City Monitor.  If not, see <http:#www.gnu.org/licenses/>.
#
# Contact: moray.grieve@me.com

import sys
from com.apama.services.scenario import *
from com.apama.services.scenario.internal import *
import java.beans.PropertyChangeEvent as PropertyChangeEvent
import java.beans.PropertyChangeListener as PropertyChangeListener
 
class ScenarioInstancePrinter():
	'''Print out instance details of a scenario definition. '''
	def __init__(self, definition, filter):
		self.instances={}
		self.params = [x for x in filter if x in definition.getOutputParameterNames()]
		for instance in definition.getInstances(): self.nhandlerImpl('ADDED:   ', instance)
	
		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_ADDED,   self.handler('ADDED:   ', self.nhandlerImpl))
		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_UPDATED, self.handler('UPDATED: ', self.nhandlerImpl))
		definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_REMOVED, self.handler('REMOVED: ', self.rhandlerImpl))
 
	def handler(self, action, impl):
		return type('',(PropertyChangeListener,),{'propertyChange':lambda this,evt : impl(action, evt.getNewValue())})()
               
	def nhandlerImpl(self, action, instance):
		self.instances[instance.getId()]=', '.join('%s=%s'%(p,str(instance.getValue(p))) for p in self.params)
		print action, self.instances[instance.getId()]
 
	def rhandlerImpl(self, action, instance):
		print action, self.instances[instance.getId()]
		del self.instances[instance.getId()]
 
 
class ScenarioDefinitionFinder():
	'''Connect to a correlator, find required definition and start the instance printer. '''
	def __init__(self, host, port, name, filter):
		self.scenarioService = ScenarioServiceFactory.createScenarioService(host, port,' ', None, self.handler(name, filter))
               
	def handler(self, name, filter):
		return type('',(PropertyChangeListener,),{'propertyChange':lambda this,evt : self.handlerImpl(evt, name, filter)})()
 
	def handlerImpl(self, evt, name, filter):
		if (IScenarioService.PROPERTY_SCENARIO_DISCOVERY_STATUS==evt.getPropertyName() and evt.getNewValue()==DiscoveryStatusEnum.COMPLETE):
			self.discovered = True
			print 'Discovery is complete'
			if hasattr(self, 'definition'): ScenarioInstancePrinter(self.definition, filter)
 
		if (IScenarioService.PROPERTY_SCENARIO_ADDED==evt.getPropertyName() and evt.getNewValue().getId()==name):
			self.definition = evt.getNewValue()
			print 'Scenario definition %s is loaded'%name
			if hasattr(self, 'discovered'): ScenarioInstancePrinter(self.definition, filter)
