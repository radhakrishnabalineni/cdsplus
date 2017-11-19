mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 < C:/Users/kashyaks/workspace/cdsp-dbscripts/config/createDBs.js

mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 -d configDB -c  loader_mappings  --drop --file C:/Users/kashyaks/workspace/cdsp-dbscripts/config/loader_mappings.json

mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 -d configDB -c  web_mappings  --drop --file C:/Users/kashyaks/workspace/cdsp-dbscripts/config/web_mappings.json

mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 -d configDB -c  processor_mappings  --drop --file C:/Users/kashyaks/workspace/cdsp-dbscripts/config/processor_mappings.json

mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 -d configDB -c  mimeTypes  --drop --file C:/Users/kashyaks/workspace/cdsp-dbscripts/config/mimeTypes.json

mongo -u  "cdspdbrw" -p  "WelcomeRW_11243" -authenticationDatabase admin -host rs0/g9t3417:20001,g9t3418:20001,g9t3384:20001 -d region -c  metadata_live  --drop --file C:/Users/kashyaks/workspace/cdsp-dbscripts/config/region.json

db.web_mappings.insert({ "_id" : "productmaster", "contentCollection" : "content", "hierarchyCollection" : "hierarchy", "mongoDB" : "productmaster", "password" : "cdspdb", "userName" : "cdspdb" })