## create all the DBs along with user creation
## two users are created per DB.
##	1. Read only user for Web application
##	2. Read write user for Loaders
##	3. Metadata collections (live, history, staging, temp

## Project specific properties

export PROJ_HOME=$PWD
export PROJ_CONF_FOLDER=$PROJ_HOME/config

## Mongo installation location
export MONGO_HOME=/opt/sasuapps/cdsplus/apps/mongodb
export MONGO_BIN=${MONGO_HOME}/bin

## Mongo DB server properties
export MONGO_SERVER_HOST=g2t1888c.austin.hp.com
export MONGO_SERVER_PORT=27017

## config DB credentials
export CONFIG_DBNAME="configDB"
export CONFIG_DBUSER="${CONFIG_DBUSER}"
export CONFIG_DBPASS="${CONFIG_DBUSER}"

echo "Mongo Installed at : $MONGO_HOME";

## creating DB instances on mongo server
echo "Creating DB Instances"

${MONGO_BIN}/mongo  --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} < ${PROJ_CONF_FOLDER}/createDBs.js

echo "DB Instances created successfully"

## load mappings collection in configDB
## drop the collection before loading the content from mappings.json file

echo "inserting data to web_mappings collection available in DB : ${MONGO_SERVER_HOST}:${MONGO_SERVER_PORT}/${CONFIG_DBNAME}"

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db configDB --collection web_mappings  -drop --file ${PROJ_CONF_FOLDER}/web_mappings.json

echo "inserting loader_mappings collection available in DB : ${MONGO_SERVER_HOST}:${MONGO_SERVER_PORT}/${CONFIG_DBNAME}"

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db configDB --collection loader_mappings  -drop --file ${PROJ_CONF_FOLDER}/loader_mappings.json

echo "inserting processor_mappings collection available in DB : ${MONGO_SERVER_HOST}:${MONGO_SERVER_PORT}/${CONFIG_DBNAME}"

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db configDB --collection processor_mappings  -drop --file ${PROJ_CONF_FOLDER}/processor_mappings.json

echo "inserting mimetypes collection available in DB : ${MONGO_SERVER_HOST}:${MONGO_SERVER_PORT}/${CONFIG_DBNAME}"

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db configDB --collection mimetypes  -drop --file ${PROJ_CONF_FOLDER}/mimetypes.json

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db region --collection metadata_live -drop --file ${PROJ_CONF_FOLDER}/region.json


/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db library --collection subscriptions -drop --file ${PROJ_CONF_FOLDER}/library_subscriptions.json

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db marketingstandard --collection subscriptions -drop --file ${PROJ_CONF_FOLDER}/marketingstandard_subscriptions.json

/opt/sasuapps/cdsplus/apps/mongodb/bin/mongoimport --host ${MONGO_SERVER_HOST} --port ${MONGO_SERVER_PORT} --username ${CONFIG_DBUSER} --password ${CONFIG_DBPASS} --db soar --collection subscriptions -drop --file ${PROJ_CONF_FOLDER}/soar_subscriptions.json

echo "CDS+ DB initialization complete"