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
* 缓存动态开启, 缓存自动刷新
* 全局搜索
* 支持 阿里云 OSS, FTP, 华为云 OBS, 本地存储, MINIO, OneDrive 国际/家庭/个人版, OneDrive 世纪互联版, 七牛云 KODO, 腾讯云 COS, 又拍云 USS.

## 快速开始

安装依赖环境:

```bash
# CentOS系统
yum install -y java-1.8.0-openjdk unzip

# Debian/Ubuntu系统
apt update
apt install -y openjdk-8-jre-headless unzip
```

> 如为更新程序, 则请先执行 `~/zfile/bin/stop.sh && rm -rf ~/zfile` 清理旧程序. 首次安装请忽略此选项.

下载项目:

```bash
cd ~
wget https://c.jun6.net/ZFILE/zfile-release.war
mkdir zfile && unzip zfile-release.war -d zfile && rm -rf zfile-release.war
chmod +x zfile/bin/*.sh
```

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

篇幅有限, 更详细的安装教程请参考: [安装文档](http://zhaojun.im/zfile-install)

访问地址:

用户前台: http://127.0.0.1:8080/#/main

初始安装: http://127.0.0.1:8080/#/install

管理后台: http://127.0.0.1:8080/#/admin


## OneDrive 使用教程.

访问地址进行授权, 获取 accessToken 和 refreshToken:


国际/家庭/个人版:

https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=09939809-c617-43c8-a220-a93c1513c5d4&response_type=code&redirect_uri=https://zfile.jun6.net/onedrive/callback&scope=offline_access%20User.Read%20Files.ReadWrite.All


世纪互联版:

https://login.chinacloudapi.cn/common/oauth2/v2.0/authorize?client_id=4a72d927-1907-488d-9eb2-1b465c53c1c5&response_type=code&redirect_uri=https://zfile.jun6.net/onedrive/china-callback&scope=offline_access%20User.Read%20Files.ReadWrite.All


然后分别填写至访问令牌和刷新令牌即可:

![http://cdn.jun6.net/2020-01-24_18-57-06.png](http://cdn.jun6.net/2020-01-24_18-57-06.png)

## 运行环境

* JDK: `1.8`
* 数据库: `h2/mysql`

## 预览

![前台首页](http://cdn.jun6.net/2020/01/29/a252a5cec7134.png)
![后台设置](http://cdn.jun6.net/2020/01/29/d5c85221bcffc.png)
![存储策略](http://cdn.jun6.net/2020/01/29/4b79bfba4e003.png)
![缓存管理](http://cdn.jun6.net/2020/01/29/60b0538e50f9f.png)

## 常见问题

### 数据库

缓存默认支持 `h2` 和 `mysql`, 前者为嵌入式数据库, 无需安装, 但后者相对性能更好.

### 默认路径

默认 H2 数据库文件地址: `~/.zfile/db/`, `~` 表示用户目录, windows 为 `C:/Users/用户名/`, linux 为 `/home/用户名/`, root 用户为 `/root/`

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
- [ ] 新功能 - 后台支持上传、编辑、删除等操作
- [ ] 新功能 - WebDav 支持
- [ ] 新功能 - Docker 支持
- [ ] 新功能 - 离线下载 (aria2)
- [ ] 体验优化 - 忽略文件列表 (正则表达式)
- [ ] 体验优化 - 自定义支持预览的文件后缀 (正则表达式)
- [ ] 架构调整 - 支持多存储策略
- [ ] 体验优化 - 一键安装脚本

## 支持作者

如果本项目对你有帮助，请作者喝杯咖啡吧。


<img src="http://cdn.jun6.net/alipay.png" width="200" height="312">
<img src="http://cdn.jun6.net/wechat.png" width="222" height="300">
