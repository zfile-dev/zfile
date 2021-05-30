package im.zhaojun.zfile.model.dto;

import im.zhaojun.zfile.model.entity.DriveConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author Zhao Jun
 * 2021/5/26 15:17
 */
@Data
@AllArgsConstructor
public class DriveListDTO {

    private List<DriveConfig> driveList;

    private Boolean isInstall;

}