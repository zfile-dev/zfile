<p align = "center">
<img alt="ZFile" src="https://cdn.jun6.net/2021/04/21/69a89344e2a84.png" height="150px">
<br><br>
基于 Java 的在线网盘程序，支持对接 S3、OneDrive、SharePoint、又拍云、本地存储、FTP、SFTP 等存储源，支持在线浏览图片、播放音视频，文本文件等文件类型。
<br><br>
<img src="https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat-square">
<img src="https://api.codacy.com/project/badge/Grade/70b793267f7941d58cbd93f50c9a8e0a">
<img src="https://img.shields.io/github/last-commit/zhaojun1998/zfile.svg?style=flat-square">
<img src="https://img.shields.io/github/downloads/zhaojun1998/zfile/total?style=flat-square">
<img src="https://img.shields.io/github/v/release/zhaojun1998/zfile?style=flat-square">
<img src="https://img.shields.io/github/commit-activity/y/zhaojun1998/zfile?style=flat-square">
<br>
<img src="https://img.shields.io/github/issues/zhaojun1998/zfile?style=flat-square">
<img src="https://img.shields.io/github/issues-closed-raw/zhaojun1998/zfile?style=flat-square">
<img src="https://img.shields.io/github/forks/zhaojun1998/zfile?style=flat-square">
<img src="https://img.shields.io/github/stars/zhaojun1998/zfile?style=flat-square">
<img src="https://img.shields.io/github/watchers/zhaojun1998/zfile?style=flat-square">
</p>

## 相关地址

预览地址: [https://zfile.vip](https://zfile.vip)

文档地址: [https://docs.zfile.vip](https://docs.zfile.vip)

社区地址: [https://bbs.zfile.vip](https://bbs.zfile.vip)

项目源码: [https://github.com/zhaojun1998/zfile](https://github.com/zhaojun1998/zfile)

前端源码: [https://github.com/zhaojun1998/zfile-vue](https://github.com/zhaojun1998/zfile-vue)

## 系统特色

* 支持文件操作：上传, 删除, 重命名, 新建文件夹. 后续还会支持移动和复制文件（详见下方**后续计划**）.
* 操作系统级的文件操作体验
  1. 支持拖拽上传和 Ctrl + V 粘贴上传文件和文件夹
  2. 支持 Ctrl + A 全选文件, 按 Esc 取消全选.
  3. 支持拖拽批量选择文件
  4. 支持按住 Shift 多选文件
  5. 支持多选文件后按 Delete 键删除文件.
  6. 按 Backspace 返回上级文件夹.
* 全新的 UI 风格, 更简洁易用.
* 支持给文件生成直链（短链，永久直链，二维码）
* 视频播放器支持调用本地软件进行下载，如迅雷、Motrix. 支持调用本地播放器播放，更好的进行视频解码： PotPlayer， IINA, VLC, nPlayer, MXPlayer(Free/Pro)
* 全新画廊模式, 支持按照瀑布流显示图片, 支持自定义 N 栏, 自定义每栏的间距
* 支持给文件夹配置 markdown 文档, 并配置显示方式, 如顶部、底部、弹窗
* 支持给文件夹设置密码
* 支持隐藏文件或文件夹
* 后台登录支持设置图片验证码和 2FA 身份认证，防止后台被暴力破解
* 支持自定义文件格式后缀, 避免系统内置的不完善导致文件无法预览.
* Docker 支持
* 自定义 JS, CSS
* 同时挂载多个存储策略
* 支持 S3 协议, 阿里云 OSS, FTP, SFTP, 华为云 OBS, 本地存储, MINIO, OneDrive 国际/家庭/个人版/世纪互联版/SharePoint, , 七牛云 KODO, 腾讯云 COS, 又拍云 USS.

## 快速开始

<details>
<summary>普通安装 (点击展开)</summary>
安装依赖环境:

```bash

# CentOS系统
yum install -y java-1.8.0-openjdk unzip
```

```bash
# Debian 9 / Ubuntu 14+
apt update
apt install -y openjdk-8-jre-headless unzip
```

```bash
# Debian 10 (Buster) 系统
apt update && apt install -y apt-transport-https software-properties-common ca-certificates dirmngr gnupg
wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add -
add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/
apt update && apt install -y adoptopenjdk-8-hotspot-jre
```


下载项目:

```bash
export ZFILE_INSTALL_PATH=~/zfile
mkdir -p $ZFILE_INSTALL_PATH && cd $ZFILE_INSTALL_PATH
wget https://c.jun6.net/ZFILE/zfile-release.war
unzip zfile-release.war && rm -rf zfile-release.war
chmod +x $ZFILE_INSTALL_PATH/bin/*.sh
```

启动项目:

```bash
 ~/zfile/bin/start.sh
```


</details>

---

<details>
<summary>Docker (点击展开)</summary>

```
docker run -d --name=zfile --restart=always \
    -p 8080:8080 \
    -v /root/zfile/db:/root/.zfile-v4/db \
    -v /root/zfile/logs:/root/.zfile-v4/logs \
    zhaojun1998/zfile
```

</details>

---

<details>
<summary>Docker Compose (点击展开)</summary>

```yml
version: '3.3'
services:
    zfile:
        container_name: zfile
        restart: always
        ports:
            - '8080:8080'
        volumes:
            - '/root/zfile/db:/root/.zfile-v4/db'
            - '/root/zfile/logs:/root/.zfile-v4/logs'
        image: zhaojun1998/zfile
```

</details>

---

篇幅有限, 更详细的安装教程及介绍请参考: [ZFile 文档](https://docs.zfile.vip)

## 预览

![前台首页](https://cdn.jun6.net/uPic/2022/07/11/eJU1B5.png)
![前台设置](https://cdn.jun6.net/uPic/2022/07/11/Y0fK7b.png)
![图片预览](https://cdn.jun6.net/uPic/2022/07/11/Iz0kxC.jpg)
![视频预览](https://cdn.jun6.net/uPic/2022/07/11/MsubMr.png)
![文本预览](https://cdn.jun6.net/2021/03/23/b00efdfb4892e.png)
![音频预览](https://cdn.jun6.net/uPic/2022/07/11/7U5IoK.png)
![管理登录](https://cdn.jun6.net/uPic/2022/07/11/U2XKcg.png)
![后台设置-站点设置](https://cdn.jun6.net/uPic/2022/07/11/9zsedD.png)
![后台设置-驱动器列表](https://cdn.jun6.net/uPic/2022/07/11/y2pFa1.png)
![后台设置-新增驱动器](https://cdn.jun6.net/uPic/2022/07/11/I1NWzF.png)


## 支持作者

如果本项目对你有帮助，请作者喝杯咖啡吧。

<img src="https://cdn.jun6.net/2021/03/27/152704e91f13d.png" width="400" alt="赞助我">

## Stargazers over time

[![starcharts stargazers over time](https://starchart.cc/zhaojun1998/zfile.svg)](https://starchart.cc/zhaojun1998/zfile.svg)

## 开发工具赞助

<a href="https://www.jetbrains.com/?from=zfile"><img src="https://cdn.jun6.net/2021/04/21/26e410d60b0b0.png?1=1" width="100px"></a>
