class Station:
	def __init__(self,id,name,lat,lng):
		self.id=id
		self.name=name
		self.lat=lat
		self.lng=lng
 
	def update(self,bikes,free,time):
		self.bikes=bikes
		self.free=free
		self.timestamp=time