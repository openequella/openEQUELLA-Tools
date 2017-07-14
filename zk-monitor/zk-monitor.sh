#!/bin/bash

if [ -z "$2" ] ; then
  echo Error - Expected 2 parms.  zkserver and zkport.  Exiting...!
  exit 147
fi

startDate=$(date +'%y-%m-%d_%H%M%S')
resFile=zk-monitor-log-zk"$1"-port"$2"-"$startDate".csv
a=0
echo Monitoring zookeeper ["$1"]  on port ["$2"] and saving to ["$resFile"]
hdr=date,ruok,
hdr="$hdr"zk_avg_latency,
hdr="$hdr"zk_max_latency,
hdr="$hdr"zk_min_latency,
hdr="$hdr"zk_packets_received,
hdr="$hdr"zk_packets_sent,
hdr="$hdr"zk_num_alive_connections,
hdr="$hdr"zk_outstanding_requests,
hdr="$hdr"zk_server_state,
hdr="$hdr"zk_znode_count,
hdr="$hdr"zk_watch_count,
hdr="$hdr"zk_ephemerals_count,
hdr="$hdr"zk_approximate_data_size,
hdr="$hdr"zk_open_file_descriptor_count,
hdr="$hdr"zk_max_file_descriptor_count,
echo "$hdr"
echo "$hdr" >> "$resFile"
while [ $a -lt 1 ]
do
   dateOut=$(date +'%y-%m-%d %H:%M:%S')
   tmpResRuok=$(echo ruok | nc "$1" "$2")
   tmpRes=$(echo mntr | nc "$1" "$2")
   zk_avg_latency=$(grep zk_avg_latency <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_max_latency=$(grep zk_max_latency <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_min_latency=$(grep zk_min_latency <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_packets_received=$(grep zk_packets_received <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_packets_sent=$(grep zk_packets_sent <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_num_alive_connections=$(grep zk_num_alive_connections <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_outstanding_requests=$(grep zk_outstanding_requests <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_server_state=$(grep zk_server_state <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_znode_count=$(grep zk_znode_count <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_watch_count=$(grep zk_watch_count <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_ephemerals_count=$(grep zk_ephemerals_count <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_approximate_data_size=$(grep zk_approximate_data_size <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_open_file_descriptor_count=$(grep zk_open_file_descriptor_count <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   zk_max_file_descriptor_count=$(grep zk_max_file_descriptor_count <<< "$tmpRes" |  awk -F'\t' '{print $2}')
   data="$dateOut","$tmpResRuok",
   data="$data""$zk_avg_latency",
   data="$data""$zk_max_latency",
   data="$data""$zk_min_latency",
   data="$data""$zk_packets_received",
   data="$data""$zk_packets_sent",
   data="$data""$zk_num_alive_connections",
   data="$data""$zk_outstanding_requests",
   data="$data""$zk_server_state",
   data="$data""$zk_znode_count",
   data="$data""$zk_watch_count",
   data="$data""$zk_ephemerals_count",
   data="$data""$zk_approximate_data_size",
   data="$data""$zk_open_file_descriptor_count",
   data="$data""$zk_max_file_descriptor_count"
   echo "$data" >> "$resFile" 
   echo "$data"
   sleep 5
done
}
