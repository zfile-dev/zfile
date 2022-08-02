package im.zhaojun.zfile.home.service.impl;

import cn.hutool.core.util.StrUtil;
import im.zhaojun.zfile.admin.model.param.IpfsParam;
import im.zhaojun.zfile.common.exception.file.StorageSourceException;
import im.zhaojun.zfile.common.exception.file.operator.GetFileInfoException;
import im.zhaojun.zfile.common.util.IpfsHelper;
import im.zhaojun.zfile.common.util.RequestHolder;
import im.zhaojun.zfile.common.util.StringUtils;
import im.zhaojun.zfile.home.model.enums.FileTypeEnum;
import im.zhaojun.zfile.home.model.enums.StorageTypeEnum;
import im.zhaojun.zfile.home.model.result.FileItemResult;
import im.zhaojun.zfile.home.service.base.ProxyTransferService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * @author zhaojun
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class IpfsServiceImpl extends ProxyTransferService<IpfsParam> {

    private IpfsHelper ipfs;

    @Override
    public void init() {
        try {
            String apiAddr = param.getApiAddr();
            ipfs = new IpfsHelper(apiAddr);
        } catch (RuntimeException e) {
            log.info("连接失败");
        }
        // 初始化存储源

//        // 校验文件夹是否存在
//        if (!file.exists()) {
//            throw new InitializeStorageSourceException("文件路径: \"" + file.getAbsolutePath() + "\"不存在, 请检查是否填写正确.");
//        }
    }


    @Override
    public List<FileItemResult> fileList(String folderPath) throws FileNotFoundException {
        // 安全检查，以 .. 或 /.. 开头的需拦截, 否则可能会获取到上层文件夹内容.
        if (StrUtil.startWith(folderPath, "..") || StrUtil.startWith(folderPath, "/..")) {
            return Collections.emptyList();
        }

        List<FileItemResult> fileItemList = new ArrayList<>();

        ArrayList<LinkedHashMap> files = null;
        try {
            files = (ArrayList) ipfs.files.ls(folderPath).get("Entries");
            if (files == null || files.size() == 0) {
                return fileItemList;
            }

            for (Map fileEntry : files) {
                String f;
                f = (String) fileEntry.get("Name");
                fileItemList.add(filenameToFileItem(f, folderPath));
            }

        } catch (IOException e) {
            throw new GetFileInfoException(storageId, folderPath, "ipfs连接失败");
        }


        return fileItemList;
    }


    @Override
    public FileItemResult getFileItem(String pathAndName) {
        return filenameToFileItem(pathAndName, "");
    }


    @Override
    public boolean newFolder(String path, String name) {
        try {
            return ipfs.files.mkdir(StringUtils.concat(path, name)).equals("");
        } catch (IOException e) {
            throw new StorageSourceException(storageId, "无法创建文件夹");
        }
    }


    @Override
    public boolean deleteFile(String path, String name) {
        String result;
        try {
            result = ipfs.files.rm(StringUtils.concat(path, name));
            if (result.equals("")) {
                return true;
            } else {
                throw new StorageSourceException(storageId, result);
            }
        } catch (IOException e) {
            throw new StorageSourceException(storageId, "操作失败");
        }
    }


    @Override
    public boolean deleteFolder(String path, String name) {
        return deleteFile(path, name);
    }


    @Override
    public boolean renameFile(String path, String name, String newName) {
        // 如果文件名没变，不做任何操作.
        if (StrUtil.equals(name, newName)) {
            return true;
        }

        String source = StringUtils.concat(path, name);
        String dest = StringUtils.concat(path, newName);
        try {

            String result = ipfs.files.mv(source, dest);
            if (!result.equals("")) {
                throw new StorageSourceException(storageId, result);
            }
            return true;
        } catch (Exception e) {
            log.error("存储源 {} 重命名文件 {} 至 {} 失败", storageId, source, newName, e);
        }
        return false;
    }


    @Override
    public boolean renameFolder(String path, String name, String newName) {
        return renameFile(path, name, newName);
    }

    @Override
    public String getUploadUrl(String path, String name, Long size) {
        return super.getUploadUrl(path, name,size);
    }


    @Override
    public StorageTypeEnum getStorageTypeEnum() {
        return StorageTypeEnum.IPFS;
    }


    @Override
    public void uploadFile(String path, InputStream inputStream) {


        try {
            ipfs.files.upload(inputStream, path);
        } catch (IOException e) {
            throw new StorageSourceException(storageId, "上传失败".concat(e.getMessage()));
        }
    }


    @Override
    public synchronized ResponseEntity<Resource> downloadToStream(String pathAndName) {

        HttpServletResponse response = RequestHolder.getResponse();
        try {
            OutputStream outputStream = response.getOutputStream();
            FileItemResult fileItem = filenameToFileItem(pathAndName, "/");
            ipfs.files.download(fileItem.getUrl(), outputStream);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("下载文件失败", e);
        }
    }

    private String getDownloadUrl(IpfsFileInfo fileInfo) {
        String domain;
        // 如果未填写下载域名，则默认使用公用网关下载.
        if (StrUtil.isEmpty(param.getDomain())) {
            domain = "https://ipfs.io/ipfs";
        } else {
            domain = StringUtils.trimEndSlashes(param.getDomain());
            domain = domain.endsWith("ipfs") ? domain : domain.concat("ipfs");
        }
        return StringUtils.concat(domain, fileInfo.getHash() + "?download=true" + "&filename=" + fileInfo.getName());
    }

    @Override
    public String getDownloadUrl(String pathAndName) {
        return getDownloadUrl(new IpfsFileInfo(pathAndName));
    }


    private FileItemResult filenameToFileItem(String filename, String folderPath) {
        FileItemResult fileItemResult = new FileItemResult();
        IpfsFileInfo fileInfo = new IpfsFileInfo(StringUtils.concat(folderPath, filename));

        fileItemResult.setType(fileInfo.getType());
        fileItemResult.setSize(fileInfo.getSize());
        fileItemResult.setName(filename);
        fileItemResult.setPath(folderPath);
        fileItemResult.setUrl(getDownloadUrl(fileInfo));

        return fileItemResult;


    }


    private class IpfsFileInfo {
        @Getter
        private String name;
        @Getter
        private Long size;
        @Getter
        private FileTypeEnum type;
        @Getter
        private String hash;

        public IpfsFileInfo(String pathAndName) {
            try {
                Map fileinfo = ipfs.files.stat(pathAndName);
                if (fileinfo.get("Type").toString().equals("directory")) {
                    this.type = FileTypeEnum.FOLDER;
                    this.size = ((Integer) fileinfo.get("CumulativeSize")).longValue();
                } else if (fileinfo.get("Type").toString().equals("file")) {
                    this.type = (FileTypeEnum.FILE);
                    this.size = ((Integer) fileinfo.get("Size")).longValue();
                }
                this.hash = fileinfo.get("Hash").toString();
                this.name = StringUtils.getFileName(pathAndName);
            } catch (IOException e) {
                throw new GetFileInfoException(storageId, this.name, "无法从ipfs获取文件信息");
            }
        }
    }

}