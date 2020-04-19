package im.zhaojun.zfile.model.support;

import im.zhaojun.zfile.model.dto.FileItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author zhaojun
 */
@Data
@AllArgsConstructor
public class FilePageModel {

    private int totalPage;

    private List<FileItemDTO> fileList;

}