# Cycle Monitor, Copyright (C) 2015  M.B.Grieve
# This file is part of the Cycle Monitor example application.
#
# Cycle Monitor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# Cycle Monitor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with City Monitor.  If not, see <http:#www.gnu.org/licenses/>.
#
# Contact: moray.grieve@me.com

import sys, json

from pysys.constants import *
from pysys.basetest import BaseTest
from pysys.utils.filereplace import replace
from pysys.utils.allocport import allocateTCPPort
from apama.iaf import IAFHelper
from apama.correlator import CorrelatorHelper
from com.jtech.filelist import FileListParser
from com.jtech.station import Station

class CycleMonitorTest(BaseTest):
	stations=[]
	
	def setup(self):
		'''Override BaseTest setup. '''
		BaseTest.setup(self)
		self.jython_classpath = os.path.join(PROJECT.JYTHON_HOME, 'jython.jar')
		self.addToJythonClassPath(os.path.join(PROJECT.APAMA_HOME,'lib','engine_client%s.jar'%PROJECT.APAMA_LIBRARY_VERSION))
		self.addToJythonClassPath(os.path.join(PROJECT.APAMA_HOME,'lib','util%s.jar'%PROJECT.APAMA_LIBRARY_VERSION))
		
	def addToJythonClassPath(self, path):
		'''Path builder for for jython. '''
		self.jython_classpath = '%s%s%s' % (self.jython_classpath, ENVSEPERATOR, path)
		
	def addStation(self,id,name,lat,lng):
		'''Add a station to the list of station, return the station object. '''
		station = Station(id,name,lat,lng)
		self.stations.append(station)
		return station
	
	def dumpStations(self, file):
		'''Write the current list of stations to file in JSON. '''
		with open(os.path.join(self.output, file), 'w') as fp:	
			json.dump([station.__dict__ for station in self.stations], fp, indent=4)

	def startCorrelator(self):
		'''Start a correlator and set logging, return correlator object handle. '''
		self.correlator = CorrelatorHelper(self)
		self.log.info('Allocating port %s for correlator'%self.correlator.port)
		self.correlator.start(logfile='correlator.log', arguments=['--inputLog',os.path.join(self.output,'correlator-input.log')])
		self.correlator.receive('correlator_input.log', channels=['com.apama.input'])
		self.correlator.receive('correlator_output.log')
		self.setApplicationLogFile(self.correlator, 'application.log', 'com.jtech')
		self.setApplicationLogLevel(self.correlator, 'DEBUG', 'com.jtech')
		
	def initialiseApplication(self, correlator):
		'''Initialise the application. '''
		parser=FileListParser(os.path.join(self.project.root, 'build-filelists.xml'), self.project.root, {'${apama.home}':PROJECT.APAMA_HOME})
		self.inject(correlator, parser.getFileList('apama.scenarios'))
		self.inject(correlator, parser.getFileList('apama.adbc'))
		self.inject(correlator, parser.getFileList('utils'))
		self.inject(correlator, parser.getFileList('configuration'))
		self.inject(correlator, parser.getFileList('source'))
		self.inject(correlator, parser.getFileList('strategy'))
		correlator.send(filenames='adbc.evt',filedir=self.output)
		self.waitForSignal('correlator_output.log', expr='com.jtech.source.Start', timeout=20)

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
					'${adbc.database-url}': database_url.replace('\\', '\\\\'),
					'${adbc.driver}': 'org.sqlite.JDBC',
					'${adbc.driver-jar}':'sqlite-jdbc-3.7.2.jar',
					'${adbc.username}':'',
					'${adbc.password}':''}

		for include in ['ADBC-static-codecs.xml', 'ADBC-static.xml', 'ADBC-application.xml', 'adbc.evt']:
			replace(os.path.join(PROJECT.root, 'config', include), os.path.join(self.output, include), replaceDict)
		
		return self.adbcAdapter.start(configdir=os.path.join(PROJECT.root, 'config'), configname='adbc.xml', 
		logfile='adbc_adapter.log', replace=replaceDict)
	
	def startCityBikesAdapter(self, correlator, city, data_url, schedule):
		'''Start IAF running the City Bikes adapter, return process handle. '''
		self.cityBikesAdapter = IAFHelper(self)
		replaceDict = {'${basedir}':'%s' % PROJECT.root,
					'${apama.home}':'%s' % PROJECT.APAMA_HOME,
					'${correlator.port}':'%d' % correlator.port,
					'${city-name}': city,
					'${data-url}': data_url,
					'${polling-schedule}': schedule}

		return self.cityBikesAdapter.start(configdir=os.path.join(PROJECT.root, 'config'), configname='city-bikes.xml', 
		logfile='city-bikes_adapter.log', replace=replaceDict)
		
	def startHTTPServer(self, dir=None):
		'''Start HTTP server serving files from output directory, setting port as self.httpPort. '''
		command = sys.executable
		displayName = 'http-server'

		if dir is None: dir=self.output
		
		instances = self.getInstanceCount(displayName)
		dstdout = os.path.join(self.output, 'http-server.out')
		dstderr = os.path.join(self.output, 'http-server.err')
		if instances: dstdout = '%s.%d' % (dstdout, instances)
		if instances: dstderr = '%s.%d' % (dstderr, instances)
		
		self.httpPort = allocateTCPPort()
		self.log.info('Allocating port %s for HTTP server'%self.httpPort)
		
		arguments = []
		arguments.append(os.path.join(PROJECT.root,'python','com','jtech','httpserver.py'))
		arguments.append('%s'%dir)
		arguments.append('%d'%self.httpPort)

		return self.startProcess(command, arguments, os.environ, self.output, BACKGROUND, 300, dstdout, dstderr, displayName)
		
	def startJython(self, script, scriptArgs=None):
		command = os.path.join(PROJECT.APAMA_COMMON_JRE, 'bin', 'java')
		displayName = 'jython'

		# set the default stdout and stderr
		instances = self.getInstanceCount(displayName)  
		dstdout = "%s/jython.out"%self.output
		dstderr = "%s/jython.err"%self.output
		if instances: dstdout  = "%s.%d" % (dstdout, instances)
		if instances: dstderr  = "%s.%d" % (dstderr, instances)

		# setup args
		args=[]
		args.extend(['-classpath', self.jython_classpath])
		args.append('-Dpython.home=%s' % PROJECT.JYTHON_HOME)
		args.append('-Dpython.path=%s' % os.path.join(PROJECT.root,'python'))
		args.append('org.python.util.jython')
		args.append(os.path.join(self.input, script))
		if scriptArgs is not None: args.extend(scriptArgs)

		# run the process and return the handle
		return self.startProcess(command, args, os.environ, self.output, BACKGROUND, 0, dstdout, dstderr, displayName)
		
	def setApplicationLogFile(self, correlator, logfile, package):
		'''Set the correlator application log file. '''
		correlator.manage(arguments=['-r', 'setApplicationLogFile %s %s'%(os.path.join(self.output,logfile),package)])
	
	def setApplicationLogLevel(self, correlator, level, package):
		'''Set the correlator application log level. '''
		correlator.manage(arguments=['-r', 'setApplicationLogLevel %s %s'%(level,package)])

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
