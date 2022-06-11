## Debian系操作系统后台守护

1. 将`zstarter.sh`复制到任意目录下，并把`RDIR`修改为zfile安装位置：

```bash
# Where does you installed zfile
RDIR=/root/zfile
```

2. 将`zfile.service`复制到`/etc/systemd/system`目录下，并修改起始脚本位置，脚本放在哪就填写到哪：

```bash
[Service]
ExecStart=/root/zstarter.sh
```

3. 启动服务并让他在后台运行

```bash
# 开机自启
systemctl enable zfile
# 后台运行
systemctl start zfile
# 终止
systemctl stop zfile
# 查看日志和状态
systemctl status zfile
# 取消开机自启
systemctl disable zfile
```