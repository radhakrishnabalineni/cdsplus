export ANT_HOME=/opt/ais/app/installations/apache-ant-1.9.1
export DFC_HOME=/opt/ais/app/installations/documentum/shared/

# check the user id
if [ "$USER" != "cdsplus" ]
then
  echo "You need to be cdsplus user to run this loader!"
  exit 1
fi

loaderType="concentra"

# now see if the loader is running
pid=`ps -ef | grep "${loaderType}loader" | grep -v grep | awk '{print $2}'`

if [ -z "$pid" ] 
then
  echo "${loaderType} Loader is not running"
else
  touch ./${loaderType}.stop

  echo "Stopping ${loaderType} loader "

  pid=`ps -ef | grep "${loaderType}loader" | grep -v grep | awk '{print $2}'`  
  
  while [ ! -z "$pid" ]
  do 
    echo "."
    sleep 2;   
    pid=`ps -ef | grep "${loaderType}loader" | grep -v grep | awk '{print $2}'`
  done
  
  echo 
  echo "${loaderType} Loader stopped"  
fi
