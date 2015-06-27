from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start server and set stations
		self.startHTTPServer()
		station = self.addStation(1,'Hyde Park',51512303,-159988)
		station.update(6,397,'2015-06-24 12:10:00')
		self.dumpStations(file='city-bikes.json')
		
		#start the application
		self.startCorrelator()
		self.startJython(script='printer.py', scriptArgs=['%d'%self.correlator.port])
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.startCityBikesAdapter(self.correlator, 'London', 'http://localhost:%d/city-bikes.json'%self.httpPort, '* * * * *')
		self.initialiseApplication(self.correlator)
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='ADDED', condition='>=1')
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.01, type=LOWER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
