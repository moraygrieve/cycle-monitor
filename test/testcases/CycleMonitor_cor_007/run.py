from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start the application
		self.startCorrelator(xclock=True)
		self.startScenarioPrinter(self.correlator, fields=['id','type'])
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.initialiseApplication(self.correlator)
		self.correlator.send('updates.evt')
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='REMOVED', condition='==1', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=2, type=UPPER_BOUNDARY')
		exprList.append('ADDED:    id=2, type=RATE_THRESHOLD')
		exprList.append('REMOVED:  id=2, type=UPPER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)

		self.assertLineCount('jython.out', expr='REMOVED', condition='==1')