# API Tester
Lightweight gradle project to run REST and SOAP API tests against an Equella institution.

Build with Gradle to import dependencies, and then invoke with:

```
-DequellaUrl=https://my.equella.url/institution -DequellaRestClientId=api-tester-client-id -DequellaRestClientSecret=apit-tester-client-id  -DequellaSoapUsername=equella-existing-user -DequellaSoapPassword=equella-existing-user-password -DequellaSoapItemUuid=item-uuid-to-be-resaved -DequellaSoapItemVersion=item-version-to-be-resaved
```