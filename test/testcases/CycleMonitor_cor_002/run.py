from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start server and set stations
		self.startHTTPServer()
		station1 = self.addStation(1,'Hyde Park',51512303,-159988)
		station2 = self.addStation(2,'Regent Street',51512304,-159980)
		station1.update(2,20,'2015-06-24 12:10:00')
		station2.update(20,2,'2015-06-24 12:10:00')
		self.dumpStations(file='city-bikes.json')
		
		#start the application
		self.startCorrelator()
		self.startScenarioPrinter(self.correlator)
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.startCityBikesAdapter(self.correlator, 'London', 'http://localhost:%d/city-bikes.json'%self.httpPort, '* * * * *')
		self.initialiseApplication(self.correlator)
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='ADDED', condition='>=2', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.09, type=LOWER_BOUNDARY')
		exprList.append('ADDED:    id=2, ratio=0.91, type=UPPER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
