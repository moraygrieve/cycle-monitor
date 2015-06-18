from pysys.constants import *
from pysys.basetest import BaseTest
from apama.correlator import CorrelatorHelper
from com.jtech.filelist import FileListParser

class CycleMonitorTest(BaseTest):
  
	def startCorrelator(self):
		correlator = CorrelatorHelper(self)
		correlator.start(logfile=logfile)

	def initialise(self, correlator):
		parser=FileListParser(os.path.join(self.project.root, 'build-filelists.xml'), self.project.root, {'${apama.home}':PROJECT.APAMA_HOME})
		self.inject(correlator, parser.getFileList('apama.scenarios'))
		self.inject(correlator, parser.getFileList('apama.adbc'))
		self.inject(correlator, parser.getFileList('utils'))
		self.inject(correlator, parser.getFileList('configuration'))
		self.inject(correlator, parser.getFileList('source'))
		self.inject(correlator, parser.getFileList('strategy'))
		
	def inject(self, correlator, fileList, **xargs):
		for entry in fileList:
			filedir,filenames=entry
			correlator.injectMonitorscript(filenames=filenames, filedir=filedir)