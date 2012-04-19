#!/bin/sh
JAVA_HOME=/opt/jdk1.7.0_03

if [ "$1" = "-h" ]; then echo "Syntax: $0 --simulation=SimulationJarFile [--config=SimulationXMLConfigFile]"; exit 1; fi
if [ "$#" = "0" ]; then echo "Syntax: $0 --simulation=SimulationJarFile [--config=SimulationXMLConfigFile]"; exit 1; fi

$JAVA_HOME/jre/bin/java -Xmx396m -classpath \
lib/org.eclipse.swt.linux64.jar:\
Siafu.jar:\
lib/json.jar:\
lib/org.apache.commons.collections-3.2.1.jar:\
lib/org.apache.commons.configuration-1.6.0.jar:\
lib/org.apache.commons.lang-2.4.0.jar:\
lib/org.apache.commons.logging-1.1.1.jar \
de.uni_hannover.dcsec.siafu.control.Siafu $1 $2 $3

