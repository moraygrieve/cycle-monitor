import sys, threading, traceback
from com.jtech.scenario_printer import ScenarioDefinitionFinder

if __name__ == '__main__':
	try:
		ScenarioDefinitionFinder('localhost',int(sys.argv[1]),'DV_StationAlert',['id','ratio','message'])
		obj = threading.Event()
		obj.wait()
	except:
		print traceback.print_exc()