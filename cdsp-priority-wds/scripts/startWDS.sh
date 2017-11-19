export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1/
	
# Validate cmd line args
function printUsage(){
    echo "Usage: $0 [wds-instance-name]"
    exit 1
}

if [[ $# -ne 1 ]];
 then
    printUsage
else
	client_name=$1
	export STDOUT_FILE_NAME=logs/${client_name}/WDSStdout.log
	export CLIENT_NAME=$client_name
	if [ ! -d "logs" ]; then
		## create logs directory
		mkdir -p logs
	fi
	cd logs
	if [ ! -d "$client_name" ]; then
		## create logs directory
		mkdir -p $client_name
	fi	
	cd ..
	pid=`ps -ef | grep java |  grep "client_name=$client_name" | awk '{print $2}'`
	if [ -z "$pid" ]
	then
	  echo "Starting wds.."
	  if [ -f "$client_name.stop" ]
	    then
	    rm $client_name.stop;
	  fi
	  nohup ${ANT_HOME}/bin/ant > ${STDOUT_FILE_NAME} 2> ${STDOUT_FILE_NAME} < /dev/null &
	  pid=`ps -ef | grep java |  grep "client_name=$client_name" | awk '{print $2}'`
	
	  while [ ! -z "$pid" ]
	  do
	    sleep 2;
	     pid=`ps -ef | grep java |  grep -i "client_name=$client_name" | awk '{print $2}'`
	  done
	
	  echo "WDS started on $pid"
	
	else
	  echo "WDS already running on $pid"
	fi			
fi