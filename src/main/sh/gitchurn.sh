#!/bin/bash
# Assumes that the first command line argument is a file with just Git revisions
# Assumes that the directory structure is:
# 	developer-activity-metrics/
#		src/main/sh/		<-- where git-interaction-churn.rb is
#	httpd/
#		git/ 			<-- httpd git repository
#		httpd-history/
#			src/main/sh/ 	<--where this script is
#
# Assume that we are in httpd,

cd git/

while IFS=',' read -r rev
do
  for file in `git show --pretty="format:" --name-only $rev`
  do
    if  echo "$file" | grep -Eq "*.[ch]$" 
    then 
      echo "$rev"
      echo "$file" 
      ../../developer-activity-metrics/src/main/sh/git-interaction-churn.rb $rev $file 
    fi
  done
done<"$1"
