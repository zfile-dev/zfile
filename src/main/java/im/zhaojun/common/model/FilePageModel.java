package im.zhaojun.common.model;

import im.zhaojun.common.model.dto.FileItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author zhaojun
 */
@Data
@AllArgsConstructor
public class FilePageModel {

    private int total;

    private int totalPage;

    private List<FileItemDTO> fileList;

}
