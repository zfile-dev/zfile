FROM debian:10-slim

ARG TARGETARCH

WORKDIR /root
EXPOSE 8080

RUN apt update -y && apt install --no-install-recommends fontconfig zstd -y && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY zfile-artifacts/zfile-linux-${TARGETARCH}/zfile/* /root/
COPY zfile-artifacts/zfile-linux-${TARGETARCH}/static/ /root/static/
COPY zfile-artifacts/zfile-linux-${TARGETARCH}/application.properties /root/

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

# 设置编码为 UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

CMD if [ -f /root/zfile.zst ]; then zstd --no-progress -d /root/zfile.zst && rm -rf /root/zfile.zst && chmod +x /root/zfile && /root/zfile --spring.config.location=file:/root/application.properties; else chmod +x /root/zfile && /root/zfile --spring.config.location=file:/root/application.properties; fi
