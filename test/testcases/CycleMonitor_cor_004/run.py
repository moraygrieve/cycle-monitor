from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start server and set stations
		self.startHTTPServer()
		station = self.addStation(1,'Hyde Park',51512303,-159988)
		station.update(2,20,'2015-06-24 12:10:00')
		self.dumpStations(file='city-bikes.json')
		
		#start the application (adapter polls on request only)
		self.startCorrelator()
		self.startScenarioPrinter(self.correlator)
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.startCityBikesAdapter(self.correlator, 'London', 'http://localhost:%d/city-bikes.json'%self.httpPort, 'none')
		self.initialiseApplication(self.correlator)
		self.waitForSignal('jython.out', expr='ADDED', condition='==1', timeout=5)
		
		#update the station and poll
		station.update(12,10,'2015-06-24 12:11:00')
		self.dumpStationsAndPoll(file='city-bikes.json')
		self.waitForSignal('jython.out', expr='REMOVED', condition='==1', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.09, type=LOWER_BOUNDARY')
		exprList.append('REMOVED:  id=1, ratio=0.09, type=LOWER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
