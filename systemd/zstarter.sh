#!/bin/bash

# Where does you installed zfile
RDIR=/root/zfile

cd $RDIR

LIB_DIR=$RDIR/WEB-INF/lib
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`
CLASSES=$RDIR/WEB-INF/classes
JAVA_MEM_OPTS=" -Djava.security.egd=file:/dev/./urandom -Dfile.encoding=utf-8 "
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Duser.timezone=GMT+08"
MAIN=im.zhaojun.zfile.ZfileApplication
java $JAVA_OPTS $JAVA_MEM_OPTS -classpath $CLASSES:$LIB_JARS $MAIN