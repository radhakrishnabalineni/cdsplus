//--------------------------------- Create perf DBs start-----------------------------------------------------
use library_perf;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history"); 
db.createCollection("subscriptions");
db.createCollection("metadata_cache");

db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use marketingstandard_perf;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("subscriptions");
db.createCollection("metadata_cache");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use soar_perf;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("subscriptions");
db.createCollection("metadata_cache");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"updateType":1},{ "name" : "updateType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use region_perf;
db.createCollection("metadata_live");
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

//--------------------------------- Create perf DBs end-----------------------------------------------------

//--------------------------------- Create perf Collectins start-----------------------------------------------------

use library

db.createCollection("metadata_staging_perf");
db.metadata_staging_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_live_perf");
db.metadata_live_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live_perf.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live_perf.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_temp_perf");

db.createCollection("metadata_history_perf");
db.metadata_history_perf.ensureIndex({"documentId":1}, {"name" : "documentID_index"});

db.createCollection("metadata_cache_perf");
db.metadata_cache_perf.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use soar 

db.createCollection("metadata_staging_perf");
db.metadata_staging_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_live_perf");
db.metadata_live_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live_perf.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live_perf.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_temp_perf");

db.createCollection("metadata_history_perf");
db.metadata_history_perf.ensureIndex({"documentId":1}, {"name" : "documentID_index"});

db.createCollection("metadata_cache_perf");
db.metadata_cache_perf.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use marketingstandard

db.createCollection("metadata_staging_perf");
db.metadata_staging_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_live_perf");
db.metadata_live_perf.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live_perf.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live_perf.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live_perf.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live_perf.ensureIndex({"updateType":1},{ "name" : "updateType_index"});

db.createCollection("metadata_temp_perf");

db.createCollection("metadata_history_perf");
db.metadata_history_perf.ensureIndex({"documentId":1}, {"name" : "documentID_index"});

db.createCollection("metadata_cache_perf");
db.metadata_cache_perf.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});

use productmaster;

db.createCollection("content_perf");
db.createCollection("hierarchy_perf");

db.content_perf.ensureIndex({"hierarchy_level" : 1},{ "name" : "hierarchyLevel_index"});
db.content_perf.ensureIndex({"node_type" : 1},{ "name" : "nodeType_index"});
db.hierarchy_perf.ensureIndex({"hierarchy_level" : 1},{ "name" : "hierarchyLevel_index"});
db.hierarchy_perf.ensureIndex({"node_type" : 1},{ "name" : "nodeType_index"});

//--------------------------------- Create perf Collectins end-----------------------------------------------------