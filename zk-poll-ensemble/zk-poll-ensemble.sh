#!/bin/bash
# Gather information about a ZK ensemble.

date
echo Querying node 1...
echo Querying [[mntr]] on node 1: 
echo mntr | nc zk_node_1_ip zk_node_1_port
echo Querying [[cons]] on node 1:
echo cons | nc zk_node_1_ip zk_node_1_port
echo Querying [[wchc]] on node 1:
echo wchc | nc zk_node_1_ip zk_node_1_port

echo Querying node 2...
echo Querying [[mntr]] on node 2:
echo mntr | nc zk_node_2_ip zk_node_2_port
echo Querying [[cons]] on node 2:
echo cons | nc zk_node_2_ip zk_node_2_port
echo Querying [[wchc]] on node 2:
echo wchc | nc zk_node_2_ip zk_node_2_port

echo Querying node 3...
echo Querying [[mntr]] on node 3:
echo mntr | nc zk_node_3_ip zk_node_3_port
echo Querying [[cons]] on node 3:
echo cons | nc zk_node_3_ip zk_node_3_port
echo Querying [[wchc]] on node 3:
echo wchc | nc zk_node_3_ip zk_node_3_port

echo ===============================================================
