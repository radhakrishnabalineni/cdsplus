#!/bin/bash

current_dir=`pwd`
instance_name=`ps -ef | grep java | grep $current_dir | awk '{for(i=1;i<=NF;i++){if(match($i,/-Dinstance_name/)){split($i,a,"=");print a[2]}}}'`

pid=`ps -ef | grep java |  grep $current_dir | awk '{print $2}'`
if [ -z "$pid" ]
then
  echo "Processor Instance $instance_name is not running"
else
  touch processor.stop

  echo "Stopping Processor Instance $instance_name..."

  pid=`ps -ef | grep java |  grep $current_dir | awk '{print $2}'`

  while [ ! -z "$pid" ]
  do
    sleep 2;
     pid=`ps -ef | grep java |  grep $current_dir | awk '{print $2}'`
  done

  echo "Processor Instance $instance_name stopped"
  rm processor.stop;
fi

