#!/bin/bash
#
# Allows a more dynamic invocation of a generalized jmx test.

#Pull parameters
propsFile=$1
lbUrl=$2
numOfUsers=$3
rampUp=$4
loopCount=$5
duration=$6
startDate=$(date +'%y-%m-%d_%H%M%S')

#Create a temp properties file and drop the supplied values into the temp properties
baseConfig="$lbUrl"_u"$numOfUsers"_r"$rampUp"_d"$duration"_lc"$loopCount"_"$startDate"
tempProps=tempRunner_"$baseConfig".properties
touch "$tempProps"
cp $propsFile $tempProps
sed 's/lbUrlPlaceholder/'$lbUrl'/g' -i "$tempProps"
sed 's/numOfUsersPlaceholder/'$numOfUsers'/g' -i "$tempProps"
sed 's/rampUpPlaceholder/'$rampUp'/g' -i "$tempProps"
sed 's/durationPlaceholder/'$duration'/g' -i "$tempProps"
sed 's/loopNumPlaceholder/'$loopCount'/g' -i "$tempProps"

resFile=testResults_"$baseConfig".jtl

#Run JMeter
JVM_ARGS="-Xms2048m -Xmx2048m" ./apache-jmeter-2.10/bin/jmeter --nongui --addprop $tempProps --logfile $resFile --testfile [your jmeter test].jmx

#Clean up
endDate=$(date +'%y-%m-%d_%H%M%S')
mv $resFile testResults_"$baseConfig"_to_"$endDate".jtl
mv tempRunner_"$baseConfig".properties tempRunner_"$baseConfig"_to_"$endDate".properties
