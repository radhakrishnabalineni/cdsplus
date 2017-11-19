#!/bin/bash

# Validate cmd line args
function printUsage(){
    echo "Usage: $0 [deploy-nostart|deploy-start]"
    exit 1
}

directory="/opt/ais/app/applications/loaders/concentra"
service=concentraloader

function deploy(){
  pid=`ps -ef | grep java | grep $service | awk '{print $2}'`
  if [ -z "$pid" ]
   then
    echo "Concentra Loader is not running"
   else
        response=`ps -ef | grep java | grep $service | awk '{for(i=1;i<=NF;i++){if(match($i,/log4j.log.location/)){print $i;break;}}}'`
        IFS='/' read -ra output <<< "$response"
        echo "Stopping concentra loader... from /opt/ais/app/applications/loaders/concentra/${output[7]}"
        cd /opt/ais/app/applications/loaders/concentra/${output[7]}
        ./stopLoader.sh
        echo "Concentra Loader is stopped"
        sleep 10;
  fi
  pid=`ps -ef | grep java | grep $service | awk '{print $2}'`
  if [ -z "$pid" ]
  then
        unzip "$directory/ConcentraLoader.zip" -d "$directory"
        cd $directory
        rm -rf "L.`date +%m.%d.%Y`"
        mv conc-loader "L.`date +%m.%d.%Y`"
        chmod -R 755 "L.`date +%m.%d.%Y`"
        echo "Deployed concentra loader....in /opt/ais/app/applications/loaders/concentra/L.`date +%m.%d.%Y`"
        cd /opt/ais/app/applications/loaders/concentra/L.`date +%m.%d.%Y`
        mkdir logs
        chmod 755 logs
        mkdir log
        chmod 755 log
        mkdir xdmsdoc
        chmod 755 xdmsdoc
        mkdir temp
        chmod 755 temp
        ln -s /opt/ais/app/applications/loaders/concentra/conf conf
   fi
}

function deploy_nostart(){
    deploy
}

function deploy_start(){
    deploy
    cd /opt/ais/app/applications/loaders/concentra/L.`date +%m.%d.%Y`
    echo "Starting concentra loader...."
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

