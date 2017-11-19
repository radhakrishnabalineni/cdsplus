export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1
export DFC_HOME=/opt/ais/app/installations/documentum/shared/

# check the user id
if [ "$USER" != "cdsplus" ]
then
  echo "You need to be cdsplus user to run this loader!"
  exit 1
fi

loaderType="soar"

# now see if the loader is running
pid=`ps -ef | grep "${loaderType}loader" | grep -v grep | awk '{print $2}'`

if [ -z "$pid" ] 
then
  echo "${loaderType} loader is not running"
else
  pauseFile="./${loaderType}.pause"
  touch ${pauseFile}
  
  echo "Waiting for ${loaderType} loader "

  while [ -f "$pauseFile" ]
  do 
    echo "."
    sleep 2;   
  done
  
  echo 
  echo "${loaderType} loader paused"  
fi