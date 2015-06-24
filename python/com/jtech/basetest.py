from pysys.constants import *
from pysys.basetest import BaseTest
from pysys.utils.filecopy import filecopy
from apama.correlator import CorrelatorHelper
from apama.iaf import IAFHelper
from com.jtech.filelist import FileListParser

class CycleMonitorTest(BaseTest):
  
	def startCorrelator(self):
		correlator = CorrelatorHelper(self)
		correlator.start(logfile='correlator.log')
		return correlator

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

	def startADBCAdapter(self, correlator, schema=None, insert=None):
		# create the sqlite store
		if not schema: schema = os.path.join(PROJECT.root,'test','utils','scripts','schema.sql')
		else:  schema = os.path.join(self.input, schema)
		
		if not insert: insert = os.path.join(PROJECT.root,'test','utils','scripts','insert.sql')
		else: insert = os.path.join(self.input, insert)

		self.sqlite(schema)
		self.sqlite(insert)

		# allocate ports and prepare the replace dictionary
		self.adbcAdapter = IAFHelper(self)
		database_url = 'jdbc:sqlite:%s' % os.path.join(self.output, 'store.db')
		replaceDict = {'${basedir}':'%s' % PROJECT.root,
					'${ADBC_DATABASE_URL}': database_url.replace('\\', '\\\\'),
					'${ADBC_DRIVER}': 'org.sqlite.JDBC',
					'${ADBC_DRIVER_JAR}':'sqlite-jdbc-3.7.2.jar',
					'${apama.home}':'%s' % PROJECT.APAMA_HOME,
					'${correlator.port}':'%d' % correlator.port }

		# copy the required static xml elements to the output directory 
		for include in ['ADBC-static-codecs.xml', 'ADBC-static.xml', 'ADBC-application.xml']:
			filecopy(os.path.join(PROJECT.root, 'config', include), os.path.join(self.output, include))
		
		# start the adapter
		return self.adbcAdapter.start(configdir=os.path.join(PROJECT.root, 'config'), configname='city-bikes-adbc.xml', 
		logfile='adbc_adapter.log', replace=replaceDict)

	def sqlite(self, filename):
		# set the command and display name
		if PLATFORM in [ "sunos", "linux" ]: command = '/usr/bin/sqlite3'
		else: command = os.path.join(PROJECT.root,'test','utils','bin','sqlite3.exe')
		displayName = 'sqlite3'

		# set the stdout and err
		instances = self.getInstanceCount(displayName)
		dstdout = os.path.join(self.output, 'sqlite.out')
		dstderr = os.path.join(self.output, 'sqlite.err')
		if instances: dstdout = '%s.%d' % (dstdout, instances)
		if instances: dstderr = '%s.%d' % (dstderr, instances)
		
		# set the arguments to the process
		arguments = []
		arguments.append('-echo')
		arguments.extend(['-init', filename])
		arguments.append('store.db')
		arguments.append('.quit')
    
		# start the process
		self.startProcess(command, arguments, os.environ, self.output, FOREGROUND, 300, dstdout, dstderr, displayName)
