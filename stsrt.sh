#!/bin/sh
# author lihao3
# ./wison-punch.sh start 启动
# ./wison-punch.sh stop 停止
# ./wison-punch.sh restart 重启
# ./wison-punch.sh status 状态
SERVER_NAME=wison-punch
APP_NAME=punch-web.jar
APP_HOME=/www/server/wison-punch/$3
LOG_PATH=$APP_HOME/logs
JVM_OPTS="-XX:HeapDumpPath=$LOG_PATH $5 $6 $7 $8 $9"
SKYWALKING_OPTS="-javaagent:/www/server/agent/skywalking-agent.jar=agent.service_name=$SERVER_NAME -Dskywalking.collector.backend_service=$2 -Dskywalking.trace.ignore_path=/exclude/*,/actuator/*"
APP_PORT=--server.port=$4

if [ "$1" = "" ]; then
  echo -e "\033[0;31m 未输入操作名 \033[0m  \033[0;34m {start|stop|restart|status} \033[0m"
  exit 1
fi
sh /www/server/wison-punch/2023-11-28~11.53.59/wison-punch.sh start 10.99.10.123:11800 2023-11-28~11.53.59 9897 -XX:+HeapDumpOnOutOfMemoryError -Xms1g -Xmx1g
if [ "$APP_NAME" = "" ]; then
  echo -e "\033[0;31m 未输入应用名 \033[0m"
  exit 1
fi

# shellcheck disable=SC2112
function start() {
  PID=$(ps -ef | grep java | grep $APP_NAME | grep -v grep | awk '{print $2}')

#  if [ x"$PID" != x"" ]; then
#    echo "$APP_NAME is running..."
#  else
    export DISPLAY=:0
    source /etc/profile
    nohup java -Dfile.encoding=utf-8 $SKYWALKING_OPTS $JVM_OPTS -jar $APP_HOME/$APP_NAME $APP_PORT > /dev/null 2>&1 &
    echo "Start $APP_NAME success..."
#  fi
}

# shellcheck disable=SC2112
function stop() {
  echo "Stop $APP_NAME"

  PID=""
  query() {
    PID=$(ps -ef | grep java | grep $APP_NAME | grep -v grep | awk '{print $2}')
  }

  query
  if [ x"$PID" != x"" ]; then
    kill -9 $PID
    echo "$APP_NAME (pid:$PID) exiting..."
    while [ x"$PID" != x"" ]; do
      sleep 1
      query
    done
    echo "$APP_NAME exited."
  else
    echo "$APP_NAME already stopped."
  fi
}

# shellcheck disable=SC2112
function restart() {
  stop
  sleep 2
  start
}

# shellcheck disable=SC2112
function status() {
  PID=$(ps -ef | grep java | grep $APP_NAME | grep -v grep | wc -l)
  if [ $PID != 0 ]; then
    echo "$APP_NAME is running..."
  else
    echo "$APP_NAME is not running..."
  fi
}

case $1 in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  restart
  ;;
status)
  status
  ;;
*) ;;

esac
