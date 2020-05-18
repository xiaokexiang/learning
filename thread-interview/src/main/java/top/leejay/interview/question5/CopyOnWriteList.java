package top.leejay.interview.question5;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * copy-on-write：写时复制 当对集合执行写操作时，内部已确保安全的数组就会被整体复制 如果频繁写会比较耗时 适合读操作多
 */
@SuppressWarnings("all")
public class CopyOnWriteList {
    private static final Object obj = new Object();

    public static void main(String[] args) {
        final CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        new Thread(() -> {
            for (int i = 0; true; i++) {
                list.add(i);
                list.remove(0);
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                synchronized (obj) {
                    // 不能使用foreach 增删list 元素
                    list.forEach(System.out::println);
                }
            }
        }).start();
    }


}
