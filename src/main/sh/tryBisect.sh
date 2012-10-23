#!/bin/bash 

if [ $# -lt 3 ];
then
    echo "usage: tryBisect <CVE number> <vulnerable file> <git fix commit> [<java class>]"
    echo "examples:"
    echo "    tryBisect 20020392 modules/http/http_protocol.c 9ca73a8515b0c9dabb09a80728295027609d92d5"
    echo "    tryBisect CVE-2002-0392 \"/modules/http/http_protocol.c\" \"9ca73a8515b0c9dabb09a80728295027609d92d5\""
    echo
    echo "    Result: 27677a4383e0c5d63d83a276ab931380976ef925 is the first bad commit"
    echo
    exit 1
fi

echo
echo "==============================================================="
echo
echo "./tryBisect.sh $1 $2 $3 $4"
echo
echo $(date)
echo
echo

#removing letters and dashes, if any, from first argument (CVE number)
CVE=${1//[CVE-]/}
echo "CVE: $CVE"
echo

#removing prefix slash, if any, from second argument (vulnerable file)
VULNERABLE_FILE=${2#/}

# Get path of this script regardless of where it is being called from.
SCRIPT_DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Get path just outside the repository
DIR=$(readlink -e $SCRIPT_DIR/../../../../)

# Assume httpd and httpd-history directories are in the same parent directory
HTTPD_PATH=$DIR/httpd
HTTPD_HISTORY_PATH=$DIR/httpd-history

cd $HTTPD_PATH

# Get absolute path for vulnerable file
FILE_PATH=$HTTPD_PATH/$VULNERABLE_FILE
echo "FILE_PATH: $FILE_PATH"
echo

# Fixed Git Commit
FIXED=$3
echo "FIXED: $FIXED"
echo

# Make sure repository is clean and updated to fixed commit.
echo "Resetting repository..."
(cd $HTTPD_PATH && git bisect reset HEAD && git checkout --merge $FIXED && git reset --hard && git clean -xdfq)
echo
echo

# If vulnerable file not found check to see if can find a file with the same name and path but in other folder
if [ ! -f $FILE_PATH ];
then
    echo
    echo "File not found: $FILE_PATH"
    echo "Trying to find alternative vulnerable file based on path segment: $VULNERABLE_FILE."
    echo
    ALT_FILE_COUNT=$(find . -wholename *$VULNERABLE_FILE | wc -l)
    if [ $ALT_FILE_COUNT -ne 1 ];
    then
        echo
        echo "Error: Alternative vulnerable file not found or ambiguous file name. Found $ALT_FILE_COUNT files with that path segment."
        echo
        exit 1
    fi    
    FILE_PATH=$(readlink -e $(find . -wholename *$VULNERABLE_FILE))
    echo "Using alternative vulnerable file: $FILE_PATH"
    echo
fi

#MODIFIED_FUNCTIONS_FIX=$(git diff "$FIXED^" "$FIXED" "$FILE_PATH" | grep -E '^(commit|@@)' | sed 's/@@.*@@//' | uniq)
#echo "MODIFIED_FUNCTIONS_FIX:"
#echo "$MODIFIED_FUNCTIONS_FIX"
echo

# Java class for test script
if [ -z $4 ]; 
then
    #CLASS=GitBisectReturnCVE$CVE;
    CLASS=GitBisectDiff;
else
    CLASS=$4;   
fi

JAVA_FILE=$HTTPD_HISTORY_PATH/src/main/java/edu/rit/se/history/httpd/intro/$CLASS.java

# If java file not found check to see if there are several files for this vulnerability
if [ ! -f $JAVA_FILE ];
then
    echo
    echo "File not found: $JAVA_FILE"
    echo "Trying to find alternative java file based on CVE code and vulnerable file name."
    echo
	# Look inside the Java file to find for references to the vulnerable file
    JAVA_FILE_COUNT=$(grep -rl --include='*$CVE*' '$VULNERABLE_FILE' * | wc -l)
    if [ $JAVA_FILE_COUNT -ne 1 ];
    then
        echo
        echo "Error: Alternative java file not found."
        echo
        exit 1
    fi        
    JAVA_FILE=$(readlink -e $(grep -rl --include='*$CVE*' '$VULNERABLE_FILE' *))
    TMP=$(basename "$JAVA_FILE")
    CLASS="${TMP%.*}"
fi

echo "Using java file: $JAVA_FILE"
echo

# Build
echo "Building java file..."
echo "javac `cygpath -wp $JAVA_FILE`"
javac `cygpath -wp $JAVA_FILE`
echo

# Bisect
echo "Bisecting..."
# Get first version of the file:
echo "git log --reverse $VULNERABLE_FILE | grep -m 1 commit"
FIRST=$(git log --reverse $VULNERABLE_FILE| grep -m 1 commit)
echo
echo "FIRST: ${FIRST:7}"
echo
# With the ^ symbol the bisect uses the immediate previous version:
echo "git bisect start $FIXED^ ${FIRST:7}^ -- $VULNERABLE_FILE"
echo
git bisect start $FIXED^ ${FIRST:7}^ -- $VULNERABLE_FILE
echo
echo
echo "git diff $FIXED^ $FIXED -- $VULNERABLE_FILE > ${VULNERABLE_FILE//\//_}_$CVE.diff"
DIFF=$(git diff $FIXED^ $FIXED -- $VULNERABLE_FILE)
if [ -z "$DIFF" ]; 
then
    echo
    echo "Error: Fix commit is not related to vulnerable file."
    echo
    exit 1
fi
echo "${DIFF}" > ${VULNERABLE_FILE//\//_}_$CVE.diff
echo
echo "git bisect run java -cp `cygpath -wp $HTTPD_HISTORY_PATH/src/main/java/` edu.rit.se.history.httpd.intro.$CLASS $VULNERABLE_FILE $CVE"
echo
echo "Result:"
echo
# Run bisect, but only show filtered results:
BISECT_RESULT=$(git bisect run java -cp `cygpath -wp $HTTPD_HISTORY_PATH/src/main/java/` edu.rit.se.history.httpd.intro.$CLASS $VULNERABLE_FILE $CVE | grep -v "^\[" | grep -v "Context not found:" | grep -i "is the first bad commit\|error\|exception\|fail")
echo "${BISECT_RESULT}"
#BAD_COMMIT=${BISECT_RESULT%% *}
#echo "BAD_COMMIT: $BAD_COMMIT"
echo
#MODIFIED_FUNCTIONS_BAD=$(git diff "$BAD_COMMIT^" "$BAD_COMMIT" "$FILE_PATH" | grep -E '^(commit|@@)' | sed 's/@@.*@@//' | uniq)
#echo "MODIFIED_FUNCTIONS_BAD:"
#echo "$MODIFIED_FUNCTIONS_BAD"
echo
echo