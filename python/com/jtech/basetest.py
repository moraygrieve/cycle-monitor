from pysys.constants import *
from pysys.basetest import BaseTest
from pysys.utils.filereplace import replace
from apama.correlator import CorrelatorHelper
from apama.iaf import IAFHelper
from com.jtech.filelist import FileListParser

class CycleMonitorTest(BaseTest):
 
	def start(self, inserts=None):
		#start the correlator and adapters
		correlator = CorrelatorHelper(self)
		correlator.start(logfile='correlator.log')
		self.settApplicationLogFile(correlator, 'application.log', 'com.jtech')
		adbc = self.startADBCAdapter(correlator, inserts)
		
		#initialise the application
		self.initialise(correlator)
		correlator.send(filenames='ADBC.evt',filedir=self.output)
		
	def initialise(self, correlator):
		parser=FileListParser(os.path.join(self.project.root, 'build-filelists.xml'), self.project.root, {'${apama.home}':PROJECT.APAMA_HOME})
		self.inject(correlator, parser.getFileList('apama.scenarios'))
		self.inject(correlator, parser.getFileList('apama.adbc'))
		self.inject(correlator, parser.getFileList('utils'))
		self.inject(correlator, parser.getFileList('configuration'))
		self.inject(correlator, parser.getFileList('source'))
		self.inject(correlator, parser.getFileList('strategy'))
		
	def settApplicationLogFile(self, correlator, logfile, package):
		correlator.manage(arguments=['-r', 'setApplicationLogFile %s %s'%(os.path.join(self.output,logfile),package)])

	def inject(self, correlator, fileList, **xargs):
		for entry in fileList:
			filedir,filenames=entry
			correlator.injectMonitorscript(filenames=filenames, filedir=filedir)

	def startADBCAdapter(self, correlator, insert=None):
		# create the sqlite store
		schema = os.path.join(PROJECT.root,'test','utils','scripts','schema.sql')
		
		if not insert: insert = os.path.join(PROJECT.root,'test','utils','scripts','insert.sql')
		else: insert = os.path.join(self.input, insert)

		self.sqlite(schema)
		self.sqlite(insert)

		# allocate ports and prepare the replace dictionary
		self.adbcAdapter = IAFHelper(self)
		database_url = 'jdbc:sqlite:%s' % os.path.join(self.output, 'store.db')
		replaceDict = {'${basedir}':'%s' % PROJECT.root,
					'${apama.home}':'%s' % PROJECT.APAMA_HOME,
					'${correlator.port}':'%d' % correlator.port,
					'${ADBC_DATABASE_URL}': database_url.replace('\\', '\\\\'),
					'${ADBC_DRIVER}': 'org.sqlite.JDBC',
					'${ADBC_DRIVER_JAR}':'sqlite-jdbc-3.7.2.jar',
					'${ADBC_USERNAME}':'',
					'${ADBC_PASSWORD}':''}

		# copy the required static xml elements to the output directory 
		for include in ['ADBC-static-codecs.xml', 'ADBC-static.xml', 'ADBC-application.xml', 'ADBC.evt']:
			replace(os.path.join(PROJECT.root, 'config', include), os.path.join(self.output, include), replaceDict)
		
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
