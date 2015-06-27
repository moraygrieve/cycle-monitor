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

import sys, SimpleHTTPServer, BaseHTTPServer
 
if __name__=="__main__":
	port = int(sys.argv[1])

	# configure httpd parameters
	server_addr = ('localhost', port)
	request_handler = SimpleHTTPServer.SimpleHTTPRequestHandler
 
	# instantiate a server object
	httpd = BaseHTTPServer.HTTPServer (server_addr, request_handler)
 
	# start serving pages
	httpd.serve_forever ()