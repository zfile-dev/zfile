# Z-File

![https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square](https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat-square)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/70b793267f7941d58cbd93f50c9a8e0a)](https://www.codacy.com/manual/zhaojun1998/zfile?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zhaojun1998/zfile&amp;utm_campaign=Badge_Grade)
![https://img.shields.io/badge/springboot-2.0.6-orange.svg?style=flat-square](https://img.shields.io/badge/springboot-2.0.6-yellow.svg?longCache=true&style=popout-square)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/zhaojun1998/zfile.svg?style=flat-square)

此项目是一个在线文件目录的程序, 支持各种对象存储和本地存储, 使用定位是个人放常用工具下载, 或做公共的文件库. 不会向多账户方向开发.

前端基于 [h5ai](https://larsjung.de/h5ai/) 的原有功能使用 Vue 重新开发了一遍. 后端采用 SpringBoot, 数据库采用内嵌数据库.

预览地址: [https://zfile.jun6.net](https://zfile.jun6.net)

文档地址: [http://docs.zhaojun.im/zfile](http://docs.zhaojun.im/zfile)

## 系统特色

* 内存缓存 (免安装)
* 内存数据库 (免安装)
* 个性化配置
* 自定义目录的 readme 说明文件
* 自定义 JS, CSS
* 文件夹密码
* 支持在线浏览文本文件, 视频, 图片, 音乐. (支持 FLV 和 HLS)
* 文件/目录二维码
* 缓存动态开启, ~~缓存自动刷新 (v2.2 及以前版本支持)~~
* ~~全局搜索 (v2.2 及以前版本支持)~~
* 同时挂载多个存储策略
* 支持 阿里云 OSS, FTP, 华为云 OBS, 本地存储, MINIO, OneDrive 国际/家庭/个人版, OneDrive 世纪互联版, 七牛云 KODO, 腾讯云 COS, 又拍云 USS.

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

![前台首页](https://cdn.jun6.net/2020/04/19/d590d2bde13bb.png)
![后台设置-驱动器设置](https://cdn.jun6.net/2020/04/19/d58fc2debcce8.png)
![后台设置-驱动器设置](https://cdn.jun6.net/2020/04/19/0f321e47fc18c.png)
![后台设置-显示设置](https://cdn.jun6.net/2020/04/19/6d7c300b89671.png)

## 常见问题


### 默认路径

默认 H2 数据库文件地址: `~/.zfile/db/`, `~` 表示用户目录

windows 为 `C:/Users/用户名/`

linux 为 `/home/用户名/`, root 用户为 `/root/`

> 2.3 及以后版本路径为 `~/.zfile-new/db/`

### 文档文件和加密文件

- 目录文档显示文件名为 `readme.md`
- 目录需要密码访问, 添加文件 `password.txt` (无法拦截此文件被下载, 但可以改名文件)

## 开发计划

- [x] API 支持 [点击查看文档](https://github.com/zhaojun1998/zfile/blob/master/API.md)
- [x] 更方便的部署方式
- [x] 布局优化 - 自定义操作按钮 (现为右键实现)
- [x] 后台优化 - 设置按照其功能进行分离
- [x] 体验优化 - 支持前后端分离部署
- [x] 体验优化 - 文本预览更换 vscode 同款编辑器 monaco editor
- [x] 新功能 - Docker 支持
- [x] 架构调整 - 支持多存储策略
- [x] 体验优化 - 忽略文件列表 (正则表达式)
- [ ] 新功能 - 后台支持上传、编辑、删除等操作
- [ ] 体验优化 - 自定义支持预览的文件后缀 (正则表达式)
- [ ] 体验优化 - 一键安装脚本

## 支持作者

如果本项目对你有帮助，请作者喝杯咖啡吧。

<img src="http://cdn.jun6.net/alipay.png" width="200" height="312">
<img src="http://cdn.jun6.net/wechat.png" width="222" height="300">
