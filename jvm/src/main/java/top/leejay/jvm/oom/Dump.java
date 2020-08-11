package top.leejay.jvm.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaokexiang
 */
public class Dump {
    /**
     * -Xmx10m 模拟堆OOM
     */
    public static void main(String[] args) {
        List<Object> list = new ArrayList<>();
        while (true) {
            list.add(new Object());
        }
    }
}
