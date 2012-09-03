#!/bin/sh

# Store parameters
CVE=$1
FIX_COMMIT=$2
BACK_COMMIT=$3
FILE=$4

echo "Bisecting $CVE, fixed $2..."

# Load the properties files
. ./bisect.properties.default #default
. ./bisect.properties #override anything

# Find the Java class
CVE_NODASHES=`echo $CVE | sed 's/-//g'`
JAVA_SRC_FILE=`find $JAVA_BISECT_SCRIPTS -name "*$CVE_NODASHES*\.java"`
JAVA_CLASS=`echo $JAVA_SRC_FILE | grep -o "[[:alnum:]_]*\.java$" | sed 's/.\{5\}$//'`
echo "Compiling this Java class: $JAVA_SRC_FILE --> $JAVA_CLASS"

javac $JAVA_SRC_FILE
HERE=`pwd`
cd $GIT_REPO
echo "Starting git bisecti on $FILE..."
git bisect start $FIX_COMMIT $BACK_COMMIT -- $FILE
echo "Running git bisect..."

git bisect run java -cp ../httpd-history/src/main/java $JAVA_PACKAGE_PREFIX.$JAVA_CLASS $FILE

cd $HERE

