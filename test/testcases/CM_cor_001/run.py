from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		correlator = self.startCorrelator();
		self.initialise(correlator)
		self.startADBCAdapter(correlator, insert='insert.sql')

	def validate(self):
		pass
