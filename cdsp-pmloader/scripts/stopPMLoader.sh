#!/bin/bash

current_dir=`pwd`
instance_name=`ps -ef | grep java | grep $current_dir | awk '{for(i=1;i<=NF;i++){if(match($i,/-Dinstance_name/)){split($i,a,"=");print a[2]}}}'`

pid=`ps -ef | grep java |  grep $instance_name | awk '{print $2}'`
if [ -z "$pid" ]
then
  echo "Pmloader Instance $instance_name is not running"
else
 touch loader.stop

  pid=`ps -ef | grep java |  grep $instance_name | awk '{print $2}'`

   while [ ! -z "$pid" ]
  do
    sleep 300;

  echo "Stopping Pmloader Instance $instance_name..."

   pid=`ps -ef | grep java |  grep $instance_name | awk '{print $2}'`

  kill -9 "$pid"

  done

  echo "Pmloader Instance $instance_name stopped"
 
 rm loader.stop;

fi
