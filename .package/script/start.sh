#!/bin/bash

# 检测是否已启动
pid=`ps -ef | grep -n zfile | grep -v grep | grep -v launch | grep -v .sh | awk '{print $2}'`
if [ -n "${pid}" ]
then
   echo "已运行在 pid：${pid}，无需重复启动！"
   exit 0
fi

# 获取当前脚本所在路径
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ZFILE_DIR=$(dirname "$DIR")

# 启动 zfile
nohup $ZFILE_DIR/zfile/zfile --spring.config.location=$ZFILE_DIR/application.properties --spring.web.resources.static-locations=file:$ZFILE_DIR/static/  >/dev/null 2>&1 &
echo '启动中...'
sleep 3s

# 输出 pid
pid=`ps -ef | grep -n zfile | grep -v grep | grep -v .sh | awk '{print $2}'`
echo "目前 PID 为: ${pid}"
