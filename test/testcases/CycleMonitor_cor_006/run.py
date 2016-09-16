from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		#start the application
		self.startCorrelator()
		self.startScenarioPrinter(self.correlator, fields=['id','ratio','message'])
		self.startADBCAdapter(self.correlator, insert='insert.sql')
		self.initialiseApplication(self.correlator)
		self.correlator.send('updates.evt')
		
		#wait for scenario printer
		self.waitForSignal('jython.out', expr='REMOVED', condition='==1', timeout=5)
		
	def validate(self):
		exprList=[]
		exprList.append('ADDED:    id=1, ratio=0.6666, message=Change of 0.13 exceeds limit .1')
		exprList.append('REMOVED:  id=1')
		self.assertOrderedGrep('jython.out', exprList=exprList)
