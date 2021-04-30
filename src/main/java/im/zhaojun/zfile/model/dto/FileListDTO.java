package im.zhaojun.zfile.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileListDTO {

    private List<FileItemDTO> files;

    private SystemFrontConfigDTO config;

}