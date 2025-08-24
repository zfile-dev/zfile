package im.zhaojun.zfile.module.storage.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import im.zhaojun.zfile.core.exception.ErrorCode;
import im.zhaojun.zfile.core.exception.core.SystemException;
import im.zhaojun.zfile.core.exception.status.NotFoundAccessException;
import im.zhaojun.zfile.core.util.FileUtils;
import im.zhaojun.zfile.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 115 文件和路径 ID 缓存服务.
 * <p>
 * 每个存储源实例持有一个此类的实例，用于隔离不同存储源的缓存。
 *
 * @author zhaojun
 */
@Slf4j
public class Open115IdCacheService {

    /**
     * 分页最大每页条数限制
     */
    private static final Integer FILE_LIST_LIMIT = 1150;

    /**
     * 文件类型: 文件
     */
    private static final String FC_FILE = "1";

    /**
     * 路径 ID 缓存
     */
    private final Map<String, String> pathIdMap = new ConcurrentHashMap<>() {{
        put("/", "0");
        put("", "0");
    }};

    /**
     * 文件 ID 缓存
     */
    private final Map<String, String> fileIdMap = new ConcurrentHashMap<>();

    /**
     * 发送带认证的 GET 请求的函数.
     * <p>
     * 参数1: 请求 URL
     * 参数2: 请求参数
     * 返回值: 响应的 JSON 对象
     */
    private final BiFunction<String, Map<String, Object>, JSONObject> sendGetRequestWithAuth;

    public Open115IdCacheService(BiFunction<String, Map<String, Object>, JSONObject> sendGetRequestWithAuth) {
        this.sendGetRequestWithAuth = sendGetRequestWithAuth;
    }

    /**
     * 获取文件 ID，如果缓存中不存在，则会尝试通过 API 获取父目录内容来缓存.
     *
     * @param fullPath          文件完整路径
     * @param throwIfNotFound   如果未找到是否抛出异常
     * @return                  文件 ID
     */
    public String getFileId(String fullPath, boolean throwIfNotFound) {
        String id = fileIdMap.get(fullPath);
        if (id != null) {
            return id;
        }

        String parentPath = FileUtils.getParentPath(fullPath);
        if (parentPath == null) {
            throw new SystemException("无法解析路径 '" + fullPath + "' 的父路径。");
        }

        cachePathAndFileId(parentPath);

        id = fileIdMap.get(fullPath);
        if (id == null && throwIfNotFound) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }
        return id;
    }

    /**
     * 获取路径 ID，如果缓存中不存在，则会尝试通过 API 获取父目录内容来缓存.
     *
     * @param fullPath          文件夹完整路径
     * @param throwIfNotFound   如果未找到是否抛出异常
     * @return                  路径 ID
     */
    public String getPathId(String fullPath, boolean throwIfNotFound) {
        String trimEndSlashes = StringUtils.trimEndSlashes(fullPath);
        String id = pathIdMap.get(trimEndSlashes);
        if (id != null) {
            return id;
        }

        String parentPath = FileUtils.getParentPath(trimEndSlashes);
        if (parentPath == null) {
            throw new SystemException("无法解析路径 '" + trimEndSlashes + "' 的父路径。");
        }

        cachePathAndFileId(parentPath);

        id = pathIdMap.get(trimEndSlashes);
        if (id == null && throwIfNotFound) {
            throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
        }
        return id;
    }

    /**
     * 缓存指定文件夹下的所有文件和子文件夹的 ID.
     *
     * @param folderPath 文件夹路径
     */
    private void cachePathAndFileId(String folderPath) {
        String pathId = getPathId(folderPath, true);
        List<String> idList = new ArrayList<>();

        int offset = 0;
        int count;
        do {
            JSONObject jsonObject = sendGetRequestWithAuth.apply("https://proapi.115.com/open/ufile/files", new JSONObject()
                    .fluentPut("cid", pathId)
                    .fluentPut("offset", offset)
                    .fluentPut("limit", FILE_LIST_LIMIT)
                    .fluentPut("show_dir", 1));

            String cid = jsonObject.getString("cid");
            if (!Objects.equals(pathId, cid)) {
                log.warn("请求的路径 ID '{}' 与返回的路径 ID '{}' 不符, 可能是 115 做了兼容处理, 返回了根目录.", pathId, cid);
                throw new NotFoundAccessException(ErrorCode.BIZ_FILE_NOT_EXIST);
            }

            JSONArray fileList = jsonObject.getJSONArray("data");
            for (int i = 0; i < fileList.size(); i++) {
                JSONObject fileItem = fileList.getJSONObject(i);

                String fid = fileItem.getString("fid");
                String fn = fileItem.getString("fn");

                String fullPath = StringUtils.concat(folderPath, fn);
                if (Objects.equals(fileItem.getString("fc"), FC_FILE)) {
                    fileIdMap.put(fullPath, fid);
                } else {
                    pathIdMap.put(fullPath, fid);
                }
                idList.add(fid);
            }

            count = jsonObject.getInteger("count");
            offset += FILE_LIST_LIMIT;
        } while (idList.size() < count);
    }

    public void putFileId(String fullPath, String id) {
        fileIdMap.put(fullPath, id);
    }

    public void putPathId(String fullPath, String id) {
        pathIdMap.put(StringUtils.trimEndSlashes(fullPath), id);
    }

    public void deleteFileId(String fullPath) {
        fileIdMap.remove(fullPath);
    }

    public void deletePathId(String fullPath) {
        String trimmedPath = StringUtils.trimEndSlashes(fullPath);
        pathIdMap.remove(trimmedPath);

        pathIdMap.entrySet().removeIf(entry -> entry.getKey().startsWith(trimmedPath));
        fileIdMap.entrySet().removeIf(entry -> entry.getKey().startsWith(trimmedPath));
    }

    public String removeFileIdByPath(String fullPath) {
        return fileIdMap.remove(fullPath);
    }

}