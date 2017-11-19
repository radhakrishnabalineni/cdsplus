#!/bin/bash

# Validate cmd line args
function printUsage(){
    echo "Usage: $0 [deploy-nostart|deploy-start]"
    exit 1
}

directory="/opt/ais/app/applications/loaders/soar"
service=soarloader

function deploy(){
    pid=`ps -ef | grep java | grep $service | awk '{print $2}'`
   if [ -z "$pid" ]
   then
    echo "Soar Loader is not running"
   else
        response=`ps -ef | grep java | grep $service | awk '{for(i=1;i<=NF;i++){if(match($i,/log4j.log.location/)){print $i;break;}}}'`
        IFS='/' read -ra output <<< "$response"
        echo "Stopping Soar loader... from /opt/ais/app/applications/loaders/soar/${output[7]}"
        cd /opt/ais/app/applications/loaders/soar/${output[7]}
        ./stopLoader.sh
        echo "Soar Loader is stopped"
        sleep 10;
   fi
   pid=`ps -ef | grep java | grep $service | awk '{print $2}'`
   if [ -z "$pid" ]
  then
        unzip "$directory/SoarLoader.zip" -d "$directory"
        cd $directory
        rm -rf "L.`date +%m.%d.%Y`"
        mv soar-loader "L.`date +%m.%d.%Y`"
        chmod -R 755 "L.`date +%m.%d.%Y`"
        echo "Starting soar loader....from /opt/ais/app/applications/loaders/soar/L.`date +%m.%d.%Y`"
        cd /opt/ais/app/applications/loaders/soar/L.`date +%m.%d.%Y`
        mkdir logs
        chmod 755 logs
        mkdir temp
        chmod 755 temp
        ln -s /opt/ais/app/applications/loaders/soar/conf conf
  fi
}

function deploy_nostart(){
    deploy
}

function deploy_start(){
    deploy
    cd /opt/ais/app/applications/loaders/soar/L.`date +%m.%d.%Y`
    echo "Starting soar loader...."
    ./startLoader.sh
}

if [[ $# -ne 1 ]]; then
    printUsage
fi
case "$1" in
    deploy-nostart)
        deploy_nostart
    ;;
    deploy-start)
        deploy_start
    ;;
    *)
        printUsage
esac

