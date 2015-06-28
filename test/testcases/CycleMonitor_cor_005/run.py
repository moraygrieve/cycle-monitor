from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start server and set stations
		self.startHTTPServer()
		station = self.addStation(2,'Regent Street',51512304,-159980)
		station.update(20,0,'2015-06-24 12:10:00')
		self.dumpStations(file='city-bikes.json')
		
		#start the application (adapter polls on request only)
		self.startCorrelator()
		self.startScenarioPrinter(self.correlator)
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.startCityBikesAdapter(self.correlator, 'London', 'http://localhost:%d/city-bikes.json'%self.httpPort, 'none')
		self.initialiseApplication(self.correlator)
		self.waitForSignal('jython.out', expr='ADDED', condition='==1', timeout=5)
		
		#update the station and poll
		station.update(15,5,'2015-06-24 12:11:00')
		self.dumpStationsAndPoll(file='city-bikes.json')
		self.waitForSignal('jython.out', expr='REMOVED', condition='==1', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=2, ratio=1.0, type=UPPER_BOUNDARY')
		exprList.append('REMOVED:  id=2, ratio=1.0, type=UPPER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
