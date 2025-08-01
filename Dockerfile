# 此文件仅作为示例使用，与 ZFile 实际打包的 Dockerfile 不同（采用 Graal Native 打包，这部分不开源）
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /root

ADD ./pom.xml pom.xml
ADD ./src src

RUN mvn clean package -Dmaven.test.skip=true

FROM ibm-semeru-runtimes:open-21-jre-jammy

WORKDIR /root
EXPOSE 8080

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

RUN apt update -y && apt install --no-install-recommends fontconfig -y && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=builder /root/target/*.jar /root/app.jar

CMD ["java", "-jar", "app.jar"]