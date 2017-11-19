#!/bin/sh
jboss_home="/opt/ais/app/jboss"
cd $jboss_home
#for file in *; do
#    $file/$file-ctl.sh status
#done
for file in *; do
        echo "Instance $file"
        echo "----------------------"
        filename=$file/$file-ctl.sh
        $file/$file-ctl.sh status
        response=$($filename status|awk '{print $4}')
        if [ "$response" = "RUNNING" ];
        then echo "Stopping JBoss instance $file..."
             $file/$file-ctl.sh stop
             sleep 3s
        fi
done
#for file in *; do
#    $file/$file-ctl.sh status
#done

cd /opt/ais/app/applications/cadence/deploy
rm cadence.war.`date +%m.%d.%Y`
cp cadence.war cadence.war.`date +%m.%d.%Y`
rm cadence.war
mv cadence.war.todeploy cadence.war

cd $jboss_home

for file in *; do
        echo "Instance $file"
        echo "----------------------"
        filename=$file/$file-ctl.sh
        $file/$file-ctl.sh status
        response=$($filename status|awk '{print $3}')
        if [ "$response" = "DOWN" ];
        then echo "Starting JBoss instance $file..."
             $file/$file-ctl.sh start
             sleep 3s
        fi
done
for file in *; do
      echo "Instance $file"
      echo "----------------------"
      $file/$file-ctl.sh status
done

