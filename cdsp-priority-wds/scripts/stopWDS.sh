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
	pid=`ps -ef | grep java |  grep "client_name=$client_name" | awk '{print $2}'`
	if [ -z "$pid" ]
	then
	 echo "WDS is not running"
	else
	 touch $client_name.stop
	 echo "Stopping WDS "
	 pid=`ps -ef | grep java |  grep "client_name=$client_name" | awk '{print $2}'`
	
	 while [ ! -z "$pid" ]
	 do
	   sleep 2;
	   pid=`ps -ef | grep java |  grep "client_name=$client_name" | awk '{print $2}'`
	 done
	 echo
	 echo "WDS stopped"
	 rm $client_name.stop;
	fi
fi

