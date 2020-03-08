package im.zhaojun.zfile.model.support;

import com.sun.management.OperatingSystemMXBean;
import lombok.Data;

import java.lang.management.ManagementFactory;

/**
 * @author zhaojun
 */
@Data
public class Mem {

    /**
     * 内存总量
     */
    private double total;

    /**
     * 已用内存
     */
    private double used;

    /**
     * 剩余内存
     */
    private double free;

    public Mem() {
        OperatingSystemMXBean osb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // 总的物理内存+虚拟内存
        long totalVirtualMemory = osb.getTotalSwapSpaceSize();
        // 剩余的物理内存
        long freePhysicalMemorySize = osb.getFreePhysicalMemorySize();
        this.total = totalVirtualMemory;
        this.free = freePhysicalMemorySize;
        this.used = totalVirtualMemory - freePhysicalMemorySize;
    }


}
