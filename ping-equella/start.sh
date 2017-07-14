#!/bin/bash

# To run as a cron job (say hourly at the 25 minute mark):
# 25 * * * * sh /usr/local/ping/start.sh
cd /usr/local/ping
theDate=$(date "+%y-%m-%d.%H.%M.%S")
/path/to/java -jar pingDriver_currentVersion.jar > output_"$theDate".txt 2>&1 &
