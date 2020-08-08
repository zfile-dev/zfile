
# Author shikeke
# 基于centos 的底包
From centos 

# copy the current directory content into the container at /project

# ADD ./ /project/zfile

# set the working directory to /project/zfile
WORKDIR /opt

# 安装相关依赖
RUN yum install -y java-1.8.0-openjdk unzip wget \
&& cd /opt \
&& wget https://c.jun6.net/ZFILE/zfile-release.war \
&& mkdir zfile && unzip zfile-release.war -d zfile && rm -rf zfile-release.war \
&& chmod +x zfile/bin/*.sh 

# make port 9999 available to the world outside this container
EXPOSE 8080

ENV LANG=en_US.utf-8

# start the project
CMD ./zfile/bin/start.sh && ./zfile/bin/restart.sh && tail -f /dev/null
