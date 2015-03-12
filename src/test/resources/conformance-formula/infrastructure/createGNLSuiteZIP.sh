#!/bin/sh
#
# createGNLSuiteZIP.sh: Creates a ZIP of the GNL suite.
#
# Copyright (c) CoreFiling S.A.R.L.
set -e

OUTPUT_DIR=`pwd`
cd `dirname $0`/../..

DATE=`date +%F`
ZIPNAME=GNL-CONF-REC-$DATE.zip
ROOT_DIRNAME=conformance-gnl

#Use temporary symlink to create appropriately named top level directory in ZIP
ln -s conformance-formula/ $ROOT_DIRNAME

zip -rq $OUTPUT_DIR/$ZIPNAME $ROOT_DIRNAME/{core_schemas/2008/generic*,index-gnl.xml,infrastructure,tests/70000\ Linkbase} --exclude "*.svn*" --exclude "*.zip"

unlink $ROOT_DIRNAME

echo "Created ZIP: "$ZIPNAME