# Z-File

![https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square](https://img.shields.io/badge/license-MIT-blue.svg?longCache=true&style=flat-square)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/70b793267f7941d58cbd93f50c9a8e0a)](https://www.codacy.com/manual/zhaojun1998/zfile?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=zhaojun1998/zfile&amp;utm_campaign=Badge_Grade)
![https://img.shields.io/badge/springboot-2.0.6-orange.svg?style=flat-square](https://img.shields.io/badge/springboot-2.0.6-yellow.svg?longCache=true&style=popout-square)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/zhaojun1998/zfile.svg?style=flat-square)

此项目是一个在线文件目录的程序, 支持各种对象存储和本地存储, 使用定位是个人放常用工具下载, 或做公共的文件库. 不会向多账户方向开发.

前端基于 [h5ai](https://larsjung.de/h5ai/) 的原有功能使用 Vue 重新开发了一遍. 后端采用 SpringBoot, 数据库采用内嵌数据库.

预览地址: [https://zfile.jun6.net](https://zfile.jun6.net)

## 系统特色

* 内存缓存 (免安装)
* 内存数据库 (免安装)
* 个性化配置
* 自定义目录的 header 和 footer 说明文件
* 文件夹密码
* 支持在线浏览文本文件, 视频, 图片, 音乐. (支持 FLV 和 HLS)
* 文件/目录二维码
* 缓存动态开启, 缓存自动刷新
* 全局搜索

## 快速开始

安装 JDK 1.8 : 

```bash
yum install -y java # 适用于 Centos 7.x
```

下载项目:

```bash
wget https://github.com/zhaojun1998/zfile/releases/download/0.5/zfile-0.5.jar
```

启动项目:

```bash
java -Djava.security.egd=file:/dev/./urandom -jar zfile-0.5.jar

## 高级启动
java -Djava.security.egd=file:/dev/./urandom -jar zfile-0.5.jar --server.port=18777

## 后台运行
nohup java -Djava.security.egd=file:/dev/./urandom -jar zfile-0.5.jar &
```

> 系统使用的是内置配置文件, 默认配置请参考: [application.yml](https://github.com/zhaojun1998/zfile/blob/master/src/main/resources/application.yml)

> **可下载此文件放置与 jar 包同目录, 此时会以外部配置文件为准, 推荐适用此方式！.**

> 所有参数都可在命令行启动时, 以类似 `--server.port=18777` 的方式强制执行, 此方式的优先级最高.

> *指定 `-Djava.security.egd=file:/dev/./urandom` 是为了防止在 Linux 环境中, 生成首次登陆生成 sessionId 取系统随机数过慢的问题.*

重要参数:
- `server.port` 为指定端口, 默认为 `8080`
- `logging.path` 为日志文件存放路径, 默认为 `${user.home}/.zfile/logs`
- `spring.datasource` 下配置了 `h2` 和 `mysql` 两种数据库的支持,  默认采用 `h2`.
- `spring.cache.type` 为指定缓存方式, 默认为 `caffeine`, 即内存缓存, 无需安装, 支持切换为 `redis`, 但需配置 `spring.redis.host` 和 `spring.redis.password` 参数后才可使用.


访问地址:

用户前台: http://127.0.0.1:8080/#/main

初始安装: http://127.0.0.1:8080/#/install

管理后台: http://127.0.0.1:8080/#/admin


## 运行环境

* JDK: `1.8`
* 缓存: `caffeine`
* 数据库: `h2/mysql`

## 常见问题

### 数据库

缓存默认支持 `h2` 和 `mysql`, 前者为嵌入式数据库, 无需安装, 但后者相对性能更好.


### 默认路径

默认 H2 数据库文件地址: `~/.zfile/db/`, `~` 表示用户目录, windows 为 `C:/Users/用户名/`, linux 为 `/home/用户名/`, root 用户为 `/root/`


### 头尾文件和加密文件

- 目录头部显示文件名为 `header.md`
- 目录底部显示文件名为 `footer.md`
- 目录需要密码访问, 添加文件 `password.txt` (无法拦截此文件被下载, 但可以改名文件)

## TODO

- 文本预览更换更好用的编辑器
- 后台支持上传、编辑、删除等操作
- API 支持
- 更方便的部署方式
