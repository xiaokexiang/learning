package top.leejay.jvm.gc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaokexiang
 */
public class JConsoleTest {
    static class OOMObject {
        public byte[] placeHolder = new byte[64 * 1024];
    }

    public static void fill(int num) {
        List<OOMObject> objects = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            objects.add(new OOMObject());
        }
        System.gc();
        System.out.println("something ...");
    }

    /**
     * -Xms100m -Xmx100m -XX:+UseSerialGC
     */
    public static void main(String[] args) {
        fill(1000);
    }
}
