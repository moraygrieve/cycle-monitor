from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start server and set stations
		self.startHTTPServer(dir=self.input)
	
		#start the application
		self.startCorrelator(url='http://localhost:%d/city-bikes.json'%self.httpPort)
		self.startScenarioPrinter(self.correlator)
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.initialiseApplication(self.correlator)
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='ADDED', condition='>=2', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.09, type=LOWER_BOUNDARY')
		exprList.append('ADDED:    id=2, ratio=0.91, type=UPPER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
