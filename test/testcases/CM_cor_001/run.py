from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		correlator = CorrelatorHelper(self)
		correlator.start()
		self.initialise(correlator)

	def validate(self):
		pass
