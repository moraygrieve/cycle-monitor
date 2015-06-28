from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start the application
		self.startCorrelator()
		self.startScenarioPrinter(self.correlator)
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.initialiseApplication(self.correlator)
		self.correlator.send('updates.evt')
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='ADDED', condition='>=2', timeout=5)
		
		#delete named alert
		self.sendLiteral(self.correlator, 'com.jtech.alert.RemoveAlert("London", 1, "LOWER_BOUNDARY")')
		self.waitForSignal('jython.out', expr='REMOVED', condition='==1', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.09, type=LOWER_BOUNDARY')
		exprList.append('ADDED:    id=2, ratio=0.91, type=UPPER_BOUNDARY')
		exprList.append('REMOVED:  id=1, ratio=0.09, type=LOWER_BOUNDARY')
		self.assertOrderedGrep('jython.out', exprList=exprList)
		
		self.assertLineCount('jython.out', expr='REMOVED', condition='==1')
