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

class Station:
	def __init__(self,id,name,lat,lng):
		self.id=id
		self.name=name
		self.lat=lat
		self.lng=lng
 
	def update(self,bikes,free,time):
		self.bikes=bikes #number of bikes in the station
		self.free=free   #number of free slots
		self.timestamp=time