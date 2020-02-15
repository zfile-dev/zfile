## API 标准

所有 API 均返回 `msg`, `code`, `data` 三个属性.

| code  |      描述      |
| :---: | :------------: |
|   0   |    请求成功    |
|  -1   |    请求失败    |
|  -2   | 文件夹需要密码 |

当 `code == 0` 时, `data` 中为请求所需数据.

当 `code != 0` 时, 应当将 `msg` 中的属性作为参考值.


## 获取文件列表

### 请求 URL

`/api/list`  `GET`

### 参数

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

## 搜索


### 请求 URL

`/api/search`  `GET`

### 参数

| 参数名 |  描述  | 是否必填 |            参考值            |
| :----: | :----: | :------: | :--------------------------: |
|  name  | 搜索值 |    是    |           模糊匹配           |
|  page  |  页数  |    否    | 默认取第一页, 每页固定 30 条 |

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

`/api/directlink`  `GET`

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

`/api/config`  `GET`

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
        "readme": null,     # 文档文件名称
        "viewConfig": {
            "siteName": "站点名称",     # 站点名称
            "infoEnable": false,        # 是否开启右侧信息框
            "searchEnable": false,      # 是否开启搜索
            "searchIgnoreCase": true,   # 搜索是否忽略大小写
            "storageStrategy": "local", # 当前启用存储引擎
            "username": "2",            # 用户名
            "domain": "http://127.0.0.1:8080",  # 域名
            "enableCache": false,               # 是否开启缓存
            "searchContainEncryptedFile": false,    # 搜索是否包含加密文件夹
            "customJs": "",             # 自定义 js 片段
            "customCss": ""             # 自定义 css 片段
        }
    }
}
```
