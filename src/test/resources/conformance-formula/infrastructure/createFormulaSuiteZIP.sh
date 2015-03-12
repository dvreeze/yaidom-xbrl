#!/bin/sh
#
# createFormulaSuiteZIP.sh: Creates a ZIP of the Formula suite.
#
# Copyright (c) CoreFiling S.A.R.L.
set -e

OUTPUT_DIR=`pwd`
cd `dirname $0`/../..

DATE=`date +%F`
ZIPNAME=Formula-CONF-REC-$DATE.zip
ROOT_DIRNAME=conformance-formula

zip -rq $OUTPUT_DIR/$ZIPNAME $ROOT_DIRNAME/{core_schemas,examples,function-registry,index-bare-filters.xml,index.xml,infrastructure,tests} --exclude "*.svn*" --exclude "*.zip" --exclude "*tests/70000*"

echo "Created ZIP: "$ZIPNAME