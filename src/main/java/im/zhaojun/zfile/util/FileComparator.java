package im.zhaojun.zfile.util;

import cn.hutool.core.comparator.CompareUtil;
import im.zhaojun.zfile.model.dto.FileItemDTO;
import im.zhaojun.zfile.model.enums.FileTypeEnum;

import java.util.Comparator;

/**
 * 文件比较器
 *
 * - 文件夹始终比文件排序高
 * - 默认按照名称排序
 * - 默认排序为升序
 * - 按名称排序不区分大小写
 *
 * @author zhaojun
 */
public class FileComparator implements Comparator<FileItemDTO> {

    private String sortBy;
    private String order;

    public FileComparator() {
    }

    public FileComparator(String sortBy, String order) {
        this.sortBy = sortBy;
        this.order = order;
    }

    @Override
    public int compare(FileItemDTO o1, FileItemDTO o2) {
        if (sortBy == null) {
            sortBy = "name";
        }

        if (order == null) {
            order = "asc";
        }
        FileTypeEnum o1Type = o1.getType();
        FileTypeEnum o2Type = o2.getType();
        NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();
        if (o1Type.equals(o2Type)) {
            int result;
            switch (sortBy) {
                case "time": result = CompareUtil.compare(o1.getTime(), o2.getTime()); break;
                case "size": result = CompareUtil.compare(o1.getSize(), o2.getSize()); break;
                default: result = naturalOrderComparator.compare(o1.getName(), o2.getName()); break;
            }
            return "asc".equals(order) ? result : -result;
        }

        if (o1Type.equals(FileTypeEnum.FOLDER)) {
            return -1;
        } else {
            return 1;
        }
    }
}