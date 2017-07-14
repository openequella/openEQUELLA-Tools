#!/bin/bash

if [ -z "$2" ] ; then
  echo Error - Expected 2 parms.  startIndex and endIndex.  Exiting...!
  exit 147
fi

resFile=bulkUsers"$1"to"$2".csv
echo "username,firstname,lastname,email,password" >> "$resFile"
COUNTER=$1
while [  $COUNTER -le $2 ]; do
   echo "test-user$COUNTER,Test, Content User $COUNTER,not-a-real-email@example.com,fakePassword" >> "$resFile" 
   let COUNTER=COUNTER+1 
done
