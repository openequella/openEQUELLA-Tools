#!/bin/bash


if [ -z "$4" ] ; then
  echo Error - Expected 2 parms.  baseDirectory, grepString, showGreppedLines, prettyPrint.  Exiting...
  exit 147
fi
totalResults=0
	
for d in $1/*/ ; do
	results=0
	if [ "$4" = "true" ] ; then
    		echo --------------------------------------------------------------------------------
		echo - "$1/$d"
		echo --------------------------------------------------------------------------------
    	echo Checking "$d"...
    fi
    for filename in $d/*.html; do
    	if [ "$4" = "true" ] ; then
    		echo For "$filename":
    	fi
    	if [ "$3" = "true" ] ; then
    		cat "$filename" | grep "$2"
    	fi
    	tempResults=$(cat "$filename" | grep "$2" | wc -l | awk '{$1=$1};1');
    	results=$(expr "$results" + "$tempResults");
    	if [ "$4" = "true" ] ; then
    		echo "# of results [$tempResults]"
    	fi
    done
    if [ "$4" = "true" ] ; then
    	echo "Total # of results for this directory:  $results"
    fi
    #Meant for a CSV reader
    if [ "$4" = "false" ] ; then
    	echo "$d,$tempResults"
    fi
    totalResults=$(expr "$totalResults" + "$results");	
done

echo "--------------------------------------------------------------------------------"
echo "- Total # of results for the parent directory:  $totalResults"
echo "--------------------------------------------------------------------------------"
    
