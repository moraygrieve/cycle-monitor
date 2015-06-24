from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		self.start(inserts='insert.sql')
		self.wait(5.0)

	def validate(self):
		pass
