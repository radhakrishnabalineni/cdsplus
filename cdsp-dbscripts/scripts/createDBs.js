use admin;
db.auth("cdspdbrw","WelcomeRW_11243");

use configDB;
db.createCollection("loader_mappings");
db.createCollection("web_mappings");
db.createCollection("processor_mappings");
db.createCollection("status");

use productsetup
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use contentfeedback;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use generalpurpose;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use library;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history"); 
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use manual;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use marketingnaconsumer;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use marketinghho;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use marketingstandard;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use support;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_temp.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use soar;
db.createCollection("metadata_staging");
db.createCollection("metadata_live");
db.createCollection("metadata_temp");
db.createCollection("metadata_history");
db.createCollection("metadata_cache");
db.createCollection("subscriptions");
db.metadata_history.ensureIndex({"documentId":1}, {"name" : "documentID_index"});
db.metadata_history.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_live.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_staging.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_staging.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_staging.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"lastModified":-1, "subscriptions" : 1},{"name":"lastModified_-1_subscriptions_1"});
db.metadata_cache.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_cache.ensureIndex({"status":1},{ "name" : "status_index"});
db.metadata_cache.ensureIndex({"subscriptions":1},{ "name" : "subscriptions_index"});
db.metadata_cache.ensureIndex({"eventType":1},{ "name" : "eventType_index"});
db.metadata_cache.ensureIndex({"hasAttachments":1},{ "name" : "hasAttachments_index"});
db.metadata_cache.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});

use productmaster;
db.createCollection("content");
db.createCollection("hierarchy");
db.createCollection("pm_extraction_logs");
db.createCollection("pm_temp");
db.content.ensureIndex({"hierarchy_level" : 1},{ "name" : "hierarchyLevel_index"});
db.content.ensureIndex({"node_type" : 1},{ "name" : "nodeType_index"});
db.hierarchy.ensureIndex({"hierarchy_level" : 1},{ "name" : "hierarchyLevel_index"});
db.hierarchy.ensureIndex({"node_type" : 1},{ "name" : "nodeType_index"});

use region;
db.createCollection("metadata_live");
db.metadata_live.ensureIndex({"lastModified":-1},{ "name" : "lastModified_index"});
db.metadata_live.ensureIndex({"priority":1},{ "name" : "priority_index"});
db.metadata_live.ensureIndex({"eventType":1},{ "name" : "eventType_index"});

use diagnostics;
db.createCollection("transactions");

use cgs
db.createCollection("metadata_live");
db.metadata_live.ensureIndex({"contentType":1},{ "name" : "contentType_index"});

use supportcontent
db.createCollection("grooms");
db.createCollection("subscriptions");

use librarycontent

use contentfeedbackcontent

use generalpurposecontent

use marketinghhocontent

use reports
db.createCollection("metadata_live");
db.metadata_live.ensureIndex({"work_group_name":1},{ "name" : "work_group_name_index"});