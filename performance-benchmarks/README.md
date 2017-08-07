# EQUELLA Performance / Benchmark Scripts

Benchmark scripts, properties, and assets are in their respective folders.  These benchmark scripts have been tested on some commercial versions of Equella.  The bulk-noop-script is broken for 6.3+, and needs some work.  Note in the rest-item-lifecycle script, there is a included.versions property the will add additional checks for later versions of EQUELLA.

## Test Setup
- [General benchmark](performance-benchmarks-general.md)
- [REST Item Lifecycle benchmark](performance-benchmarks-rest-item-lifecycle.md)

## Test Institutions
Benchmark institutions have been developed on 6.2-QA1 to provide a consist data set to benchmark EQUELLA against.

Benchmark Institution
* Based off of the 6.2 blank institution
* Contains 1000 jmeteruserX users.
* Contains a General Items collection to test concurrent item contributions, and a Test Items collection for specific items needed for scripts

Small Benchmark Institution specifics:
* Contains ~40 items

Big Benchmark Institution specifics:
* Contains 50,000 users in the 'Shared Secret Users' group
* Contains ~50,000 items
* Contains lots of audit logs

After importing either benchmark institution:
* Ensure the freetext boolean operator is set to OR
* [For 6.3+] Run Generate Missing Thumbnails


