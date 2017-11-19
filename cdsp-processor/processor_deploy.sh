#!/bin/bash

# Validate cmd line args
function printUsage(){
    echo "Usage: $0 [installation-folder-name]"
    exit 1
}

if [[ $# -ne 1 ]];
 then
    printUsage
else
	installationfoldername=$1
	directory="/opt/ais/app/applications/processor"
	installationpath=$directory/$installationfoldername
	
	pid=`ps -ef | grep java | grep $installationpath | awk '{print $2}'`
	if [ -z "$pid" ]
	 then
    	echo "Processor Instance in folder $installationfoldername is not running"
	 else
        echo "Stopping processor... from /opt/ais/app/applications/processor/$installationfoldername"
        cd /opt/ais/app/applications/processor/$installationfoldername
        ./stopProcessor.sh
        echo "Processor running in $installationpath is stopped"
        sleep 10;
	fi
	pid=`ps -ef | grep java | grep $installationpath | awk '{print $2}'`
	 if [ -z "$pid" ]
	 then
        unzip "$directory/Processor.zip" -d "$directory"
        cd $directory
        rm -rf "${installationfoldername}_L.`date +%m.%d.%Y`"
        mv "${installationfoldername}" "${installationfoldername}_L.`date +%m.%d.%Y`" 
        cd $directory
        echo "Moving cdsp-processor to ${directory}/${installationfoldername}"
        mv cdsp-processor ${installationfoldername}
        chmod -R 755 "${installationfoldername}"
        echo "Deployed processor....in /opt/ais/app/applications/processor/${installationfoldername}"
        echo "Note --> edit instance_name in build.xml of this instance before starting this processor instance"
        cd /opt/ais/app/applications/processor/${installationfoldername}
        mkdir logs
        chmod 755 logs
	 fi	   
fi

