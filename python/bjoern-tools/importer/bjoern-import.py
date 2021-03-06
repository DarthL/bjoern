#!/usr/bin/env python3

import sys
import os

BASEDIR = os.path.dirname(__file__)
OCTOPUS_PYLIB = 'octopus-pylib'
OCTOPUS_PYLIB_DIR = os.path.join(BASEDIR, 'python', OCTOPUS_PYLIB)
sys.path.append(OCTOPUS_PYLIB_DIR)

from importer.OctopusImporter import OctopusImporter


class BjoernRadareImporter(OctopusImporter):

    def __init__(self):
        self.importerPluginJSON ="""{
        "plugin": "radareimporter.jar",
        "class": "bjoern.plugins.radareimporter.RadareImporterPlugin",
        "settings": {
        "projectName": "%s",
        }}
        """

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
