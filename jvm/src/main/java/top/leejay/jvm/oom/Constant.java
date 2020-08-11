package top.leejay.jvm.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaokexiang
 */
public class Constant {

    /**
     * -XX:MaxMetaspaceSize=25m -Xmx10m
     */
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        int i = 0;
        while (true) {
            list.add(("hello" + i++).intern());
        }
    }
}
