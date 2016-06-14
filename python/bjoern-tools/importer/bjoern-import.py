#!/usr/bin/env python3

import sys
import os

BASEDIR = os.path.dirname(__file__)
OCTOPUS_PYLIB = 'octopus-pylib'
OCTOPUS_PYLIB_DIR = os.path.join(BASEDIR, 'python', OCTOPUS_PYLIB)
sys.path.append(OCTOPUS_PYLIB_DIR)

from importer.OctopusImporter import OctopusImporter

importerPluginJSON ="""{
    "plugin": "radareimporter.jar",
    "class": "bjoern.plugins.radareimporter.RadareImporterPlugin",
    "settings": {
        "projectName": "%s",
    }
}
"""

class BjoernRadareImporter(OctopusImporter):

    def executeImporterPlugin(self):
        print('Executing importer plugin')
        conn = self._getConnectionToServer()
        conn.request("POST", "/executeplugin/", importerPluginJSON % (self.projectName))
        response = conn.getresponse()

def main(filename):
    importer = BjoernRadareImporter()
    importer.importFile(filename)

def usage():
    print('%s <filename>' % (sys.argv[0]))

if __name__ == '__main__':

    if len(sys.argv) != 2:
        usage()
        exit()

    main(sys.argv[1])
