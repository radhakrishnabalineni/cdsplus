export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1
export DFC_HOME=/opt/ais/app/installations/documentum/shared/
export STDOUT_FILE_NAME=logs/soarPLSysout.log

## set Documentum libraries to classpath
. ${DFC_HOME}/dfc/set_dctm_env.sh


# check the user id
if [ "$USER" != "cdsplus" ]
then
  echo "You need to be cdsplus user to run this loader!"
  exit 1
fi

echo "Output being redirected to ${STDOUT_FILE_NAME}";

loaderType="soar"

pid=`ps -ef | grep java |  grep "${loaderType}loader" | awk '{print $2}'`
if [ -z "$pid" ]
then
  echo "Starting loader.."
  if [ -f "./${loaderType}.stop" ]
    then
    rm ./${loaderType}.stop;
  fi
  nohup ${ANT_HOME}/bin/ant start > ${STDOUT_FILE_NAME} 2> ${STDOUT_FILE_NAME} < /dev/null &
  pid=`ps -ef | grep java |  grep "${loaderType}loader" | awk '{print $2}'`

  while [ ! -z "$pid" ]
  do
    sleep 2;
     pid=`ps -ef | grep java |  grep -i "${loaderType}loader" | awk '{print $2}'`
  done

  echo
  echo "${loaderType} Loader Started - $pid"

else
  echo "${loaderType} Loader already running on $pid";
fi
