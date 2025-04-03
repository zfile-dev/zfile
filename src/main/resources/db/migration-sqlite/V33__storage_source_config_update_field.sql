UPDATE
    storage_source_config
SET
    name = 'proxyPrivate'
WHERE
    type NOT IN ('huawei', 'doge-cloud', 'aliyun', 's3', 'qiniu', 'minio', 'tencent')
AND
    name = 'isPrivate';

UPDATE
    storage_source_config
SET
    name = 'proxyTokenTime'
WHERE
    type NOT IN ('huawei', 'doge-cloud', 'aliyun', 's3', 'qiniu', 'minio', 'tencent')
  AND
    name = 'tokenTime';

UPDATE
    storage_source_config
SET
    name = 'proxyLimitSpeed'
WHERE
    type NOT IN ('aliyun', 'tencent')
  AND
    name = 'limitSpeed';