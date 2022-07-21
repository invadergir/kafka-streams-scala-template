#!/bin/bash

set -x
THISDIR=$(dirname $(readlink -e ${BASH_SOURCE[0]}))

echo "USER = $(whoami)"
echo "Running jar file: $JARFILE..."
java -Dconfig.file="$THISDIR/application.conf" -jar $JARFILE

