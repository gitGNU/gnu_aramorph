#!/bin/sh
# ----------------------------------------------------------------------------
# build.sh - Linux/Unix Build Script for Aramorph
#
# Pierre Dittgen 29/04/2004
# ----------------------------------------------------------------------------
if [ "$JAVA_HOME" == "" ]; then
	echo "Fatal: JAVA_HOME env variable not found!!!"
	echo "Please set it to your Java installation path"
	echo "Can't go further... exiting."
	exit 1
fi

echo "Using Java from $JAVA_HOME"
export PATH=$JAVA_HOME/bin:$PATH

# ----- Use Ant shipped with AraMorph. Ignore installed in the system Ant
export ANT_HOME=tools

antScript=$ANT_HOME/bin/ant
if [ -f $antScript -a ! -x $antScript ]; then
	chmod u+x $antScript
fi

$antScript $*

