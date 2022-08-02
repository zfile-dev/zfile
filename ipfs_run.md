# IPFS 节点搭建

## 下载安装

```shell
wget https://dist.ipfs.io/kubo/v0.14.0/kubo_v0.14.0_linux-amd64.tar.gz -O ./go-ipfs_linux-amd64.tar.gz
tar -xvf ./go-ipfs_linux-amd64.tar.gz
```

## 初始化
zfile 需要和Ipfs节点搭建在同一机器上

在zfile的存储源设置中设置网关为http://ip:8082
```shell
export PATH=$PATH:$PWD/go-ipfs/
ipfs init
# zfile使用8080端口，所以将ipfs端口改为8082
# 将127.0.0.1 修改为 监听的ip 一般需要对外提供服务的需要设置为 0.0.0.0 或公网ip
ipfs config Addresses.Gateway /ip4/127.0.0.1/tcp/8082
```

## 启动，需添加进程守护

```shell
ipfs daemon
```
可以使用
```shell
ipfs daemon &
```
