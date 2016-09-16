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

import os, sys, xml.dom.minidom

def split_path(p):
	a,b = os.path.split(p)
	return (split_path(a) if len(a) and len(b) else []) + [b]

class FileListParser:
	def __init__(self, xmlfile, basepath, replacedict=None):
		if replacedict is None: replacedict = {}

		self.dirname = os.path.dirname(xmlfile)
		self.xmlfile = xmlfile
		self.basepath = basepath
		self.replacedict = replacedict

		if not os.path.exists(xmlfile):
			raise Exception, "Unable to find requested file list \"%s\"" % xmlfile
		
		try:
			self.doc = xml.dom.minidom.parse(xmlfile)
		except:
			raise Exception, "%s " % (sys.exc_info()[1])
		else:
			if self.doc.getElementsByTagName('project') == []:
				raise Exception, "No <project> element supplied in XML descriptor"
			else:
				self.root = self.doc.getElementsByTagName('project')[0]

	def unlink(self):
		if self.doc: self.doc.unlink()

	def getFileList(self, filelistid):
		list = []
		filelistNodeList = self.root.getElementsByTagName('filelist')
		if filelistNodeList != []:
			for filelistNode in filelistNodeList:
				id = filelistNode.getAttribute('id')
				if filelistid == id:
					fileNodeList = filelistNode.getElementsByTagName('file')
					if fileNodeList != []:
						for fileNode in fileNodeList:
							name_paths=split_path(fileNode.getAttribute('name'))
							for key in self.replacedict.keys():
								name_paths=[el.replace('%s'%(key), '%s' % (self.replacedict[key])) for el in name_paths]
							name=os.path.join(*name_paths)
							if not os.path.isabs(name):
								name=os.path.join(self.basepath, name)
							if len(list) > 0 and list[-1][0] == os.path.dirname(name):
								list[-1][1].append(os.path.basename(name))
							else:
								list.append( (os.path.dirname(name), [os.path.basename(name)]) )
		return list

