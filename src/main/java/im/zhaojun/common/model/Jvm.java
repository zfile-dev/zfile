package im.zhaojun.common.model;

import lombok.Data;

/**
 * @author zhaojun
 */
@Data
public class Jvm {

    /**
     * 当前 JVM 占用的内存总数 (M)
     */
    private double total;

    /**
     * JVM 最大可用内存总数 (M)
     */
    private double max;

    /**
     * JVM 空闲内存 (M)
     */
    private double free;

    /**
     * JDK 版本
     */
    private String version;

    public Jvm() {
        Runtime runtime = Runtime.getRuntime();
        this.total = runtime.totalMemory();
        this.free = runtime.freeMemory();
        this.max = runtime.maxMemory();
        this.version = System.getProperty("java.version");
    }

}
