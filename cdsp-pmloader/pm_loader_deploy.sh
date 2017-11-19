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
        directory="/opt/ais/app/applications/loaders/pmaster"
        installationpath=$directory/$installationfoldername

        pid=`ps -ef | grep java | grep $installationpath | awk '{print $2}'`
        if [ -z "$pid" ]
         then
        echo "Pmloader Instance in folder $installationfoldername is not running"
         else
        echo "Stopping pmloader... from /opt/ais/app/applications/loaders/pmaster/$installationfoldername"
        cd /opt/ais/app/applications/loaders/pmaster/$installationfoldername
        ./stopPMLoader.sh
        echo "Pmloader running in $installationpath is stopped"
        sleep 10;
        fi
        pid=`ps -ef | grep java | grep $installationpath | awk '{print $2}'`
         if [ -z "$pid" ]
         then
        unzip "$directory/PMLoader.zip" -d "$directory"
        cd $directory
        rm -rf "${installationfoldername}_L.`date +%m.%d.%Y`"
        mv "${installationfoldername}" "${installationfoldername}_L.`date +%m.%d.%Y`"
        cd $directory
        echo "Moving cdsp-pmaster to ${directory}/${installationfoldername}"
        mv pmaster ${installationfoldername}
        chmod -R 755 "${installationfoldername}"
        echo "Deployed pmloader....in /opt/ais/app/applications/loaders/pmaster/${installationfoldername}"
        echo "Note --> edit instance_name in build.xml of this instance before starting this pmloader instance"
        cd /opt/ais/app/applications/loaders/pmaster/${installationfoldername}
        mkdir logs
        chmod 755 logs

        cd  /opt/ais/app/applications/loaders/pmaster/

        #!/bin/bash

# Validate cmd line args
function printUsage(){
    echo "Usage: $0 [pmaster_hpi|pmaster_hpe]"
    exit 1
}

directory="/opt/ais/app/applications/loaders/pmaster/"

function deployi(){

        cd $directory

        mv $directory/pmaster_hpi/config/pmloader-sql.properties.hpi $directory/pmaster_hpi/config/pmloader-sql.properties
        mv $directory/pmaster_hpi/config/pmloader.properties.hpi $directory/pmaster_hpi/config/pmloader.properties

        rm $directory/pmaster_hpi/config/pmloader-sql.properties.hpe
        rm $directory/pmaster_hpi/config/pmloader.properties.hpe

        echo "renamed pmaster_hpi properties files and removed pmaster_hpe files"

}

function deploye(){

        cd $directory

        mv $directory/pmaster_hpe/config/pmloader-sql.properties.hpe $directory/pmaster_hpe/config/pmloader-sql.properties
        mv $directory/pmaster_hpe/config/pmloader.properties.hpe $directory/pmaster_hpe/config/pmloader.properties

        rm $directory/pmaster_hpe/config/pmloader-sql.properties.hpi
        rm $directory/pmaster_hpe/config/pmloader.properties.hpi

         echo "renamed pmaster_hpe properties files and removed pmaster_hpi files"


}

function deploy_hpi(){
    deployi
}

function deploy_hpe(){
    deploye

}

if [[ $# -ne 1 ]]; then
    printUsage
fi
case "$1" in
      pmaster_hpi)
        deploy_hpi
    ;;
     pmaster_hpe)
        deploy_hpe
    ;;
    *)
        printUsage
esac


         fi
fi
