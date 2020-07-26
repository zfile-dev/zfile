## API 标准

所有 API 均返回 `msg`, `code`, `data` 三个属性.

| code  |      描述      |
| :---: | :------------: |
|   0   |    请求成功    |
|  -1   |    请求失败    |
|  -2   | 文件夹需要密码 |

当 `code == 0` 时, `data` 中为请求所需数据.

当 `code != 0` 时, 应当将 `msg` 中的内容作为参考值.


## 驱动器列表

### 请求 URL

`/api/drive/list`  `GET`

### 响应

```json
{
    "msg": "操作成功",
    "code": 0,
    "data": [
        {
            "id": 3,                        --- 此 ID 是驱动器 ID, 用来唯一区分驱动器
            "name": "演示 A 盘",            --- 驱动器名称
            "enableCache": true,            --- 是否开启了缓存
            "autoRefreshCache": false,      --- 是否开启了缓存自动刷新
            "type": {                       --- 存储源类型
                "key": "upyun",
                "description": "又拍云 USS"
            },
            "searchEnable": false,          --- 是否开启搜索
            "searchIgnoreCase": false,      --- 搜索是否忽略大小写
            "searchContainEncryptedFile": false     --- 搜索是否包含加密文件夹
        }
    ]
}
```



## 获取文件列表

### 请求 URL

`/api/list/{driveId}`  `GET`


### URL 参数


| 参数名  |         描述          | 是否必填 |                 参考值                 |
| :-----: | :-------------------: | :------: | :------------------------------------: |
| driveId | 驱动器 ID |    是    | 参考 `获取驱动器列表` 接口返回的 id 值 |


### 请求参数


|  参数名  |    描述    | 是否必填 |            参考值            |
| :------: | :--------: | :------: | :--------------------------: |
|   path   |    路径    |    是    |      `/`, `/文件夹名称`      |
| password | 文件夹密码 |    否    |     当文件夹需要密码时,      |
|   page   |    页数    |    否    | 默认取第一页, 每页固定 30 条 |


### 响应


```json
{
    "msg": "操作成功",
    "code": 0,
    "data": [
        {
            "name": "密码文件夹",
            "time": "2020-01-28 13:17",
            "size": 4096,
            "type": "FOLDER",
            "path": "/",
            "url": null
        },
        {
            "name": "新建 文本文档.txt",
            "time": "2020-01-28 13:16",
            "size": 3,
            "type": "FILE",
            "path": "/",
            "url": "http://127.0.0.1:8080/file/新建 文本文档.txt"
        }
    ]
}
```


## 获取单个文件信息

### 请求 URL

`/api/directlink/{driveId}`  `GET`


### URL 参数

| 参数名  |         描述          | 是否必填 |                 参考值                 |
| :-----: | :-------------------: | :------: | :------------------------------------: |
| driveId | 驱动器 ID |    是    | 参考 `获取驱动器列表` 接口返回的 id 值 |

### 参数

| 参数名 |    描述    | 是否必填 |        参考值        |
| :----: | :--------: | :------: | :------------------: |
|  path  | 文件全路径 |    是    | `/新建 文本文档.txt` |

### 响应

```json
{
    "msg": "操作成功",
    "code": 0,
    "data": {
        "name": "新建 文本文档.txt",
        "time": "2020-01-28 13:16",
        "size": 3,
        "type": "FILE",
        "path": "d:/test",
        "url": "http://127.0.0.1:8080/file/新建 文本文档.txt"
    }
}
```

## 获取系统配置


### 请求 URL

`/api/config/{driveId}`  `GET`


### URL 参数

| 参数名  |         描述          | 是否必填 |                 参考值                 |
| :-----: | :-------------------: | :------: | :------------------------------------: |
| driveId | 驱动器 ID |    是    | 参考 `获取驱动器列表` 接口返回的 id 值 |


### 参数

| 参数名 |    描述    | 是否必填 |    参考值     |
| :----: | :--------: | :------: | :-----------: |
|  path  | 文件夹名称 |    是    | `/文件夹名称` |

### 响应

```json
{
    "msg": "操作成功",
    "code": 0,
    "data": {
        "siteName": "ZFile 演示站",
        "searchEnable": false,
        "username": "zhao",
        "domain": "https://zfile.jun6.net",
        "customJs": "",
        "customCss": "",
        "tableSize": "small",
        "showOperator": true,
        "showDocument": true,
        "announcement": "本站是 ZFile 演示站，交流反馈群 180605017",
        "showAnnouncement": true,
        "layout": "full",
        "readme": null
    }
}
```