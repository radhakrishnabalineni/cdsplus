export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1/
export LOADER_STDOUT_FILE_NAME=logs/validator.log

## create logs directory
mkdir logs

echo "Output being redirected to ${LOADER_STDOUT_FILE_NAME}";
echo "classpath - ${CLASSPATH}"
## start the loader job

nohup ${ANT_HOME}/bin/ant validate > ${LOADER_STDOUT_FILE_NAME} 2> ${LOADER_STDOUT_FILE_NAME} < /dev/null &
