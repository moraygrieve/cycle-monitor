import sys, threading, traceback
from com.jtech.scenario_printer import ScenarioDefinitionFinder

if __name__ == '__main__':
	port=int(sys.argv[1])
	dvname=sys.argv[2]
	fields=list(sys.argv[3].split(','))
	try:
		ScenarioDefinitionFinder('localhost',port,dvname,fields)
		obj = threading.Event()
		obj.wait()
	except:
		print traceback.print_exc()