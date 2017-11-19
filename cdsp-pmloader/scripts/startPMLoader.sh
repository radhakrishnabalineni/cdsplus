#!/bin/bash

export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1/
export STDOUT_FILE_NAME=logs/pmloaderStdout.log

## create logs directory
mkdir -p logs

current_dir=`pwd`
instance_name=`ps -ef | grep java | grep $current_dir | awk '{for(i=1;i<=NF;i++){if(match($i,/-Dinstance_name/)){split($i,a,"=");print a[2]}}}'`

pid=`ps -ef | grep java |  grep $current_dir | awk '{print $2}'`
if [ -z "$pid" ]
then
  echo "Starting pmloader Instance $instance_name..."
  if [ -f "pmloader.stop" ]
    then
    rm pmloader.stop;
  fi
  nohup ${ANT_HOME}/bin/ant execute > ${STDOUT_FILE_NAME} 2> ${STDOUT_FILE_NAME} < /dev/null &
  pid=`ps -ef | grep java |  grep $current_dir | awk '{print $2}'`

  while [ ! -z "$pid" ]
  do
    sleep 2;
     pid=`ps -ef | grep java |  grep -i $current_dir | awk '{print $2}'`
  done

  echo "pmloader Instance $instance_name started on $pid"

else
  echo "pmloader Instance $instance_name already running on $pid"
fi
