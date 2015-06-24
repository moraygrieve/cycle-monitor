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