# Cycle Monitor, Copyright (C) 2016  M.B.Grieve
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

import os, sys, SimpleHTTPServer, BaseHTTPServer
import posixpath
import urllib

class RequestHandler(SimpleHTTPServer.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        root = os.getcwd()
        for pattern, rootdir in ROUTES:
            if path.startswith(pattern):
                path = path[len(pattern):]
                root = rootdir
                break
        
        path = path.split('?',1)[0]
        path = path.split('#',1)[0]
        path = posixpath.normpath(urllib.unquote(path))
        words = path.split('/')
        words = filter(None, words)
        
        path = root
        for word in words:
            drive, word = os.path.splitdrive(word)
            head, word = os.path.split(word)
            if word in (os.curdir, os.pardir):
                continue
            path = os.path.join(path, word)

        return path

if __name__=="__main__":
	print "Initiating HTTP server"
	global ROUTES
	ROUTES=(['',sys.argv[1]],)
	port = int(sys.argv[2])
	
	# configure httpd parameters
	server_addr = ('localhost', port)
	request_handler = RequestHandler
 
	# instantiate a server object
	httpd = BaseHTTPServer.HTTPServer (server_addr, request_handler)
	print "Started HTTP server"
 
	# start serving pages
	httpd.serve_forever ()