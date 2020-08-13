package top.leejay.jvm.gc;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;

/**
 * @author xiaokexiang
 * 虚引用在被回收的实现通知
 */
public class Phantom {

    public static void main(String[] args) {
        String hello = new String("hello");
        ReferenceQueue<String> queue = new ReferenceQueue<>();
        PhantomReference<String> reference = new PhantomReference<>(hello, queue);
        new Thread(() -> {
            while (true) {
                Reference<? extends String> poll = queue.poll();
                if (poll != null) {
                    try {
                        Field referent = Reference.class.getDeclaredField("referent");
                        referent.setAccessible(true);
                        String str = (String) referent.get(poll);
                        System.out.println("GC Will Collect " + str);
                        break;
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // 去除hello的引用
        hello = null;
        System.gc();
    }
}
