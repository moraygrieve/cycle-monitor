from pysys.constants import *
from pysys.basetest import BaseTest
from pysys.utils.filereplace import replace
from pysys.utils.allocport import allocateTCPPort
from apama.iaf import IAFHelper
from apama.correlator import CorrelatorHelper
from com.jtech.filelist import FileListParser
from com.jtech.filelist import Station

class CycleMonitorTest(BaseTest):
	stations=[]
	
	def addStation(self,id,name,lat,ln):
		station = Station(id,name,lat,lng)
		station.append(station)
		return station
	
	def dumpStations(self):
		with open(os.path.join(self.output, "city-bikes.json"), "w") as fp:
			for station in station:
				json.dump([station.__dict__], fp, indent=4)

	def startCorrelator(self):
		'''Start a correlator and set logging, return correlator object handle. '''
		correlator = CorrelatorHelper(self)
		correlator.start(logfile='correlator.log')
		self.settApplicationLogFile(correlator, 'application.log', 'com.jtech')
		return correlator

	def initialiseApplication(self, correlator):
		'''Initialise the application. '''
		parser=FileListParser(os.path.join(self.project.root, 'build-filelists.xml'), self.project.root, {'${apama.home}':PROJECT.APAMA_HOME})
		self.inject(correlator, parser.getFileList('apama.scenarios'))
		self.inject(correlator, parser.getFileList('apama.adbc'))
		self.inject(correlator, parser.getFileList('utils'))
		self.inject(correlator, parser.getFileList('configuration'))
		self.inject(correlator, parser.getFileList('source'))
		self.inject(correlator, parser.getFileList('strategy'))
		correlator.send(filenames='ADBC.evt',filedir=self.output)
		self.waitForSignal('application.log', expr='loaded', timeout=20)

	def startADBCAdapter(self, correlator, insert=None):
		'''Start IAF running the ADBC adapter for sqlite, return process handle. '''
		schema = os.path.join(PROJECT.root,'test','utils','scripts','schema.sql')
		
		if not insert: insert = os.path.join(PROJECT.root,'test','utils','scripts','insert.sql')
		else: insert = os.path.join(self.input, insert)

		self.sqlite(schema)
		self.sqlite(insert)

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

		for include in ['ADBC-static-codecs.xml', 'ADBC-static.xml', 'ADBC-application.xml', 'ADBC.evt']:
			replace(os.path.join(PROJECT.root, 'config', include), os.path.join(self.output, include), replaceDict)
		
		return self.adbcAdapter.start(configdir=os.path.join(PROJECT.root, 'config'), configname='city-bikes-adbc.xml', 
		logfile='adbc_adapter.log', replace=replaceDict)

	def startHTTPServer(self):
		'''Start HTTP server serving files from output directory, setting port as self.httpPort. '''
		command = sys.executable
		displayName = 'http-server'

		instances = self.getInstanceCount(displayName)
		dstdout = os.path.join(self.output, 'http-server.out')
		dstderr = os.path.join(self.output, 'http-server.err')
		if instances: dstdout = '%s.%d' % (dstdout, instances)
		if instances: dstderr = '%s.%d' % (dstderr, instances)
		
		self.httpPort = allocateTCPPort()
		self.log.info('Allocating port %s for HTTP server'%self.httpPort)
		
		arguments = []
		arguments.append(os.path.join(PROJECT.root,'python','com','jtech','httpserver.py'))
		arguments.append('%d'%self.httpPort)

		return self.startProcess(command, arguments, os.environ, self.output, BACKGROUND, 300, dstdout, dstderr, displayName)
		
	def settApplicationLogFile(self, correlator, logfile, package):
		'''Set the correlator application logfile. '''
		correlator.manage(arguments=['-r', 'setApplicationLogFile %s %s'%(os.path.join(self.output,logfile),package)])

	def inject(self, correlator, fileList, **xargs):
		'''Inject a file list. '''
		for entry in fileList:
			filedir,filenames=entry
			correlator.injectMonitorscript(filenames=filenames, filedir=filedir)

	def sqlite(self, filename):
		'''Run sqlite using the input script specified. '''
		command = PROJECT.SQLITE3_EXE
		displayName = 'sqlite3'

		instances = self.getInstanceCount(displayName)
		dstdout = os.path.join(self.output, 'sqlite.out')
		dstderr = os.path.join(self.output, 'sqlite.err')
		if instances: dstdout = '%s.%d' % (dstdout, instances)
		if instances: dstderr = '%s.%d' % (dstderr, instances)
		
		arguments = []
		arguments.append('-echo')
		arguments.extend(['-init', filename])
		arguments.append('store.db')
		arguments.append('.quit')
    
		self.startProcess(command, arguments, os.environ, self.output, FOREGROUND, 300, dstdout, dstderr, displayName)
