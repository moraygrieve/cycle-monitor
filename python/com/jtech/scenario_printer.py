import sys, threading, traceback

from com.apama.services.scenario import * 
from com.apama.services.scenario.internal import *
import java.beans.PropertyChangeEvent as PropertyChangeEvent
import java.beans.PropertyChangeListener as PropertyChangeListener
  
class ScenarioInstanceListener(PropertyChangeListener):
	def __init__(self, printer, type):
		PropertyChangeListener.__init__(self)
		self.printer = printer
		self.type = type

	def propertyChange(self, evt):
		self.printer.pprint(self.type, [evt.getNewValue()])
 
class ScenarioInstancePrinter():
	def __init__(self, definition):
		self.definition = definition
		self.params = definition.getOutputParameterNames()
		self.pprint('ADDED', self.definition.getInstances())
		
		self.definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_ADDED, ScenarioInstanceListener(self,'ADDED'))
		self.definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_UPDATED, ScenarioInstanceListener(self,'UPDATED'))
		self.definition.addListener(ScenarioDefinition.PROPERTY_INSTANCE_REMOVED, ScenarioInstanceListener(self,'REMOVED'))

	def pprint(self, type, instances):
		for instance in instances:
			pprint={}
			for param in self.params: pprint[param]=instance.getValue(param)
			print type, pprint
 
class ScenarioDefinitionFinder(PropertyChangeListener):
	def __init__(self, host, port, name):
		PropertyChangeListener.__init__(self)
		self.name = name
		self.definition = None
		self.discovered = False
		self.scenarioService = ScenarioServiceFactory.createScenarioService(host,port,"",None,self)
 
	def propertyChange(self, evt):
		if (IScenarioService.PROPERTY_SCENARIO_DISCOVERY_STATUS==evt.getPropertyName()):
			if evt.getNewValue() == DiscoveryStatusEnum.COMPLETE:
				self.discovered = True
				if self.definition != None: 
					print "The discovery process is complete"
					ScenarioInstancePrinter(self.definition)
 
		if (IScenarioService.PROPERTY_SCENARIO_ADDED==evt.getPropertyName() ):
			if evt.getNewValue().getId() == self.name:
				self.definition = evt.getNewValue()
				if self.discovered: 
					print "Received scenario added property change for %s " % self.name
					ScenarioInstancePrinter(self.definition)

if __name__ == "__main__":
	try:
		ssPrinter = ScenarioDefinitionFinder('localhost',15903,'DV_StationAlert')
		obj = threading.Event()
		obj.wait()
	except:
		print traceback.print_exc()