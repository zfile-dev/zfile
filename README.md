# Z-File

![https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square](https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat-square)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/70b793267f7941d58cbd93f50c9a8e0a)](https://www.codacy.com/manual/zhaojun1998/zfile?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zhaojun1998/zfile&amp;utm_campaign=Badge_Grade)
![https://img.shields.io/badge/springboot-2.0.6-orange.svg?style=flat-square](https://img.shields.io/badge/springboot-2.0.6-yellow.svg?longCache=true&style=popout-square)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/zhaojun1998/zfile.svg?style=flat-square)

此项目是一个在线文件目录的程序, 支持各种对象存储和本地存储, 使用定位是个人放常用工具下载, 或做公共的文件库. 不会向多账户方向开发.

前端基于 [h5ai](https://larsjung.de/h5ai/) 的原有功能使用 Vue 重新开发、后端采用 SpringBoot, 数据库采用内嵌数据库.

预览地址: [https://zfile.jun6.net](https://zfile.jun6.net)

文档地址: [http://docs.zhaojun.im/zfile](http://docs.zhaojun.im/zfile)

## 系统特色

* Docker 支持
* 文件数据库 (免安装)
* 直链功能
* 图片模式
* 文件夹密码
* 忽略文件夹
* 自定义 JS, CSS
* 自定义目录的 readme 说明文件
* 支持在线浏览文本文件, 视频, 图片, 音乐. (支持 FLV 和 HLS)
* 文件/目录二维码
* 同时挂载多个存储策略
* 缓存动态开启, ~~缓存自动刷新 (v2.2 及以前版本支持)~~
* ~~全局搜索 (v2.2 及以前版本支持)~~
* 支持 S3 协议, 阿里云 OSS, FTP, 华为云 OBS, 本地存储, MINIO, OneDrive 国际/家庭/个人版/世纪互联版/SharePoint, , 七牛云 KODO, 腾讯云 COS, 又拍云 USS.

## 快速开始

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


> 如为更新程序, 则请先执行 `~/zfile/bin/stop.sh && rm -rf ~/zfile` 清理旧程序. 首次安装请忽略此选项.


下载项目:

```bash
cd ~
wget https://c.jun6.net/ZFILE/zfile-release.war
mkdir zfile && unzip zfile-release.war -d zfile && rm -rf zfile-release.war
chmod +x zfile/bin/*.sh
```

> 下载指定版本可以将 `zfile-release.war` 改为 `zfile-x.x.war`，如 `zfile-2.2.war`。

程序的目录结构为:
```
├── zfile
    ├── META-INF
    ├── WEB-INF
    └── bin
        ├── start.sh    # 启动脚本
        └── stop.sh     # 停止脚本
        ├── restart.sh  # 重启脚本
```

启动项目:

```bash
 ~/zfile/bin/start.sh
```

篇幅有限, 更详细的安装教程及介绍请参考: [ZFile 文档](http://docs.zhaojun.im/zfile)

访问地址:

用户前台: http://127.0.0.1:8080/#/main

初始安装: http://127.0.0.1:8080/#/install

管理后台: http://127.0.0.1:8080/#/admin


## 预览

![前台首页](https://cdn.jun6.net/2021/03/23/c1f4631ee2de4.png)
![图片预览](https://cdn.jun6.net/2021/03/23/713741d43b939.png)
![视频预览](https://cdn.jun6.net/2021/03/23/9c724383bb506.png)
![文本预览](https://cdn.jun6.net/2021/03/23/b00efdfb4892e.png)
![音频预览](https://cdn.jun6.net/2021/03/23/d15b14378d3f0.png)
![后台设置-驱动器列表](https://cdn.jun6.net/2021/03/23/b4f76f20ea73a.png)
![后台设置-新增驱动器](https://cdn.jun6.net/2021/03/23/e70e04f8cc5b6.png)
![后台设置-站点设置](https://cdn.jun6.net/2021/03/23/fd946991bb6b9.png)

## 开发计划

- [x] API 支持 [点击查看文档](https://github.com/zhaojun1998/zfile/blob/master/API.md)
- [x] 更方便的部署方式
- [x] 布局优化 - 自定义操作按钮 (现为右键实现)
- [x] 后台优化 - 设置按照其功能进行分离
- [x] 体验优化 - 支持前后端分离部署
- [x] 体验优化 - 文本预览更换 vscode 同款编辑器 monaco editor
- [x] 架构调整 - 支持多存储策略
- [x] 体验优化 - 忽略文件列表 (正则表达式)
- [x] 新功能 - Docker 支持
- [x] 新功能 - 图片模式
- [x] 新功能 - 直链/短链管理
- [ ] ~~新功能 - 后台支持上传、编辑、删除等操作 （不再支持）~~
- [ ] 体验优化 - 自定义支持预览的文件后缀 (正则表达式)
- [ ] 体验优化 - 一键安装脚本
- [ ] 新功能 - 分享功能，支持分享密码，文件夹分享
- [ ] 新功能 - 直链支持 Referer 防盗链
- [ ] 体验优化 - 视频列表支持
- [ ] 新功能 - 单独页面打开文件预览
- [ ] 新功能 - 在线查看日志功能
- [ ] 部署优化 - Docker Compose 支持

## 支持作者

如果本项目对你有帮助，请作者喝杯咖啡吧。

<img src="https://cdn.jun6.net/2021/03/27/152704e91f13d.png" width="400" alt="赞助我">

## 项目 star 趋势

[![starcharts stargazers over time](https://starchart.cc/zhaojun1998/zfile.svg)](https://starchart.cc/zhaojun1998/zfile.svg)

## 服务器赞助

[云联小白](http://www.mcmc.pro/)
