package im.zhaojun.zfile.core.util;

import cn.hutool.core.comparator.CompareUtil;
import im.zhaojun.zfile.module.storage.model.result.FileItemResult;
import im.zhaojun.zfile.module.storage.model.enums.FileTypeEnum;

import java.util.Comparator;

/**
 * 文件比较器
 * <ul>
 *     <li>文件夹始终比文件排序高</li>
 *     <li>默认按照名称排序</li>
 *     <li>默认排序为升序</li>
 *     <li>按名称排序不区分大小写</li>
 * </ul>
 * @author zhaojun
 */
public class FileComparator implements Comparator<FileItemResult> {

    private String sortBy;

    private String order;

    public FileComparator(String sortBy, String order) {
        this.sortBy = sortBy;
        this.order = order;
    }


    /**
     * 比较两个文件的大小
     *
     * @param   o1
     *          第一个文件
     *
     * @param   o2
     *          第二个文件
     *
     * @return  比较结果
     */
    @Override
    public int compare(FileItemResult o1, FileItemResult o2) {
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
            int result = switch (sortBy) {
                case "time" -> CompareUtil.compare(o1.getTime(), o2.getTime());
                case "size" -> CompareUtil.compare(o1.getSize(), o2.getSize());
                default -> naturalOrderComparator.compare(o1.getName(), o2.getName());
            };
            return "asc".equals(order) ? result : -result;
        }

        if (o1Type.equals(FileTypeEnum.FOLDER)) {
            return -1;
        } else {
            return 1;
        }
    }

}