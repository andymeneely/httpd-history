#!/bin/bash 
set -e
bold=`tput bold`
normal=`tput sgr0`

if [ $# -ne 3 ];
then
    echo "${bold}usage:${normal} tryBisect <CVE number> <vulnerable file> <git fix commit>"
    echo "${bold}examples:${normal}"
    echo "    tryBisect 20020392 modules/http/http_protocol.c 9ca73a8515b0c9dabb09a80728295027609d92d5"
    echo "    tryBisect CVE-2002-0392 \"/modules/http/http_protocol.c\" \"9ca73a8515b0c9dabb09a80728295027609d92d5\""
    echo
    echo "    Result: 27677a4383e0c5d63d83a276ab931380976ef925 is the first bad commit"
    echo
    exit 1
fi

echo "${normal}"
echo
echo "-----------------------------------------------------------"
echo

#removing letters and dashes, if any, from first argument (CVE number)
CVE=${1//[CVE-]/}
echo "${bold}CVE:${normal} $CVE"
echo

#removing prefix slash, if any, from second argument (vulnerable file)
FILE=${2#/}

# Get path of this script regardless of where it is being called from.
SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Get path just outside the repository
DIR=$(readlink -e $SCRIPT_DIR/../../../../)

# Assume httpd and httpd-history directories are in the same parent directory
HTTPD_PATH=$DIR/httpd
HTTPD_HISTORY_PATH=$DIR/httpd-history

# Get absolute path for vulnerable file
FILE_PATH=$HTTPD_PATH/$FILE
echo "${bold}FILE_PATH:${normal} $FILE_PATH"
echo

# If vulnerable file not found check to see if can find a file with the same name and path but in other folder
if [ ! -f $FILE_PATH ];
then
    echo
    echo "File not found: $FILE_PATH"
    echo "Trying to find alternative vulnerable file based on path segment: $FILE."
    echo
    ALT_FILE_COUNT=$(find . -wholename *$FILE | wc -l)
    if [ $ALT_FILE_COUNT -ne 1 ];
    then
        echo "Error: Alternative vulnerable file not found or ambiguous file name. Found $ALT_FILE_COUNT files with that path segment."
        echo
        exit 1
    fi    
    FILE_PATH=$(readlink -e $(find . -wholename *$FILE))
    echo "Using alternative vulnerable file: $FILE_PATH"
    echo
fi

# Fixed Git Commit
FIXED=$3
echo "${bold}FIXED:${normal} $FIXED"
echo
cd $HTTPD_PATH
MODIFIED_FUNCTIONS_FIX=$(git diff "$FIXED^" "$FIXED" "$FILE_PATH" | grep -E '^(commit|@@)' | sed 's/@@.*@@//' | uniq)
echo "${bold}MODIFIED_FUNCTIONS_FIX:${normal}"
echo "$MODIFIED_FUNCTIONS_FIX"
echo

# Java class for test script
CLASS=GitBisectReturnCVE$CVE

JAVA_FILE=$HTTPD_HISTORY_PATH/src/main/java/edu/rit/se/history/httpd/intro/$CLASS.java

# If java file not found check to see if there are several files for this vulnerability
if [ ! -f $JAVA_FILE ];
then
    echo
    echo "File not found: $JAVA_FILE"
    echo "Trying to find alternative java file based on CVE code and vulnerable file name."
    echo
	# Look inside the Java file to find for references to the vulnerable file
    JAVA_FILE_COUNT=$(grep -rl --include='*$CVE*' '$FILE' * | wc -l)
    if [ $JAVA_FILE_COUNT -ne 1 ];
    then
        echo "Error: Alternative java file not found."
        echo
        exit 1
    fi        
    JAVA_FILE=$(readlink -e $(grep -rl --include='*$CVE*' '$FILE' *))
    TMP=$(basename "$JAVA_FILE")
    CLASS="${TMP%.*}"
    echo "Using alternative java file: $JAVA_FILE"
    echo
fi

# Build
echo "${bold}Building java file...${normal}"
javac $JAVA_FILE
echo

# Bisect
echo "${bold}Bisecting...${normal}"
git checkout .
git clean  -d  -fx ""
git bisect reset HEAD
# Get first version of the file:
FIRST=$(git log --reverse $FILE_PATH | grep -m 1 commit)
echo
# With the ^ symbol the bisect uses the immediate previous version:
git bisect start $FIXED^ ${FIRST:7} -- $FILE_PATH
echo
echo
echo "${bold}Result:"
echo
# Run bisect, but only show filtered results:
BISECT_RESULT=$(git bisect run java -cp $HTTPD_HISTORY_PATH/src/main/java/ edu.rit.se.history.httpd.intro.$CLASS "$FILE_PATH" | grep -v "^\[" | grep -i "is the first bad commit\|error\|exception\|fail")
echo $BISECT_RESULT
BAD_COMMIT=${BISECT_RESULT%% *}
#echo "BAD_COMMIT: $BAD_COMMIT"
echo "${normal}"
MODIFIED_FUNCTIONS_BAD=$(git diff "$BAD_COMMIT^" "$BAD_COMMIT" "$FILE_PATH" | grep -E '^(commit|@@)' | sed 's/@@.*@@//' | uniq)
echo "${bold}MODIFIED_FUNCTIONS_BAD:${normal}"
echo "$MODIFIED_FUNCTIONS_BAD"
echo
echo

# add to src/main/sh
