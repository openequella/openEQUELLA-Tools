# Generate Expected Search Results
## Purpose
Sometimes the Equella search indices can be inconsistent.  This script is 
to provide insight as to the _expected_ search results, not necessarily in 
order, but containing all items in the institution.

* The username used to run this script must have DISCOVER_ITEM privileges on all 
items in the institution.
* You need to setup an OAuth Implicit Grant with a default redirect URL.

## Key Inputs
db.item.dump <<< path to a csv containing all items in the DB in the format:
	id, date_for_index, status, uuid, version, thumb
	
	This historically has been pulled with a simple BIRT report
db.target.institution <<< ID of the institution (only the csv items from db.item.dump
	 with a matching institution_id will be considered)

## Outputs
expected_gallery_results.csv
* Calls /api/item/uuid/version?info=attachment
* If the item json item/attachments/attachment/thumbnail is not in ("","suppress', null) and the original file is not a PDF, then GV thumbs should be required
* If GV thumbs are required, calls /api/item/uuid/version/file and ensures the _135_.jpeg thumb is present.

expected_standard_results.csv
* All items should be in the standard results.

```
JVM_ARGS="-Xms2048m -Xmx2048m" /path/to/jmeter --addprop generate-expected-search-results.properties --testfile generate-expected-search-results.jmx -l results.jtl
```
