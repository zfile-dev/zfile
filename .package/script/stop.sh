#!/bin/bash

echo "------------------ 检测状态 START --------------"
pid=`ps -ef | grep -n zfile | grep -v grep | grep -v .sh | awk '{print $2}'`
if [ -z "${pid}" ]
then
   echo "未运行, 无需停止!"
else
   echo "运行pid：${pid}"
   kill -9 ${pid}
   echo "已停止进程: ${pid}"
fi

echo "------------------ 检测状态  END  --------------"