# Cycle Monitor, Copyright (C) 2016  M.B.Grieve
# This file is part of the Cycle Monitor example application.

# Cycle Monitor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# Cycle Monitor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with City Monitor.  If not, see <http://www.gnu.org/licenses/>.

# Contact: moray.grieve@me.com

import sys, threading, traceback
from com.jtech.scenario_printer import ScenarioDefinitionFinder

if __name__ == '__main__':
	port=int(sys.argv[1])
	dvname=sys.argv[2]
	fields=list(sys.argv[3].split(','))
	try:
		ScenarioDefinitionFinder('localhost',port,dvname,fields)
		r=raw_input("Press any key to stop ...")
	except:
		print traceback.print_exc()