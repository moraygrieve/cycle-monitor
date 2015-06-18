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

