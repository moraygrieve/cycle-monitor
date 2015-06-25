from pysys.constants import *
from apama.correlator import CorrelatorHelper
from com.jtech.basetest import CycleMonitorTest

class PySysTest(CycleMonitorTest):
	def execute(self):
		server = self.startHTTPServer()
		station = self.addStation(1,'Hyde Park',51512303,-159988)
		station.update(6,397,'2015-06-24 12:10:00'))
		self.dumpStations()

		correlator = self.startCorrelator()
		adbc = self.startADBCAdapter(correlator, insert='insert.sql')
		self.initialiseApplication(correlator)

	def validate(self):
		pass
