package top.leejay.interview.question5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * ArrayList的线程安全API：synchronizedList
 * List不能使用foreach 增加删除元素
 */
@SuppressWarnings("all")
public class SynchronizedList {
    private static final Object obj = new Object();

    public static void main(String[] args) {
        // final 修饰的list表明不能被重新赋值(list = new ArrayList()) 并不代表该不能被修改(list.add(1))
        final List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());

        new Thread(() -> {
            for (int i = 0; true; i++) {
                synchronizedList.add(i);
                synchronizedList.remove(0);
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                synchronized (obj) {
                    // 不能使用foreach 增删list 元素
                    synchronizedList.forEach(System.out::println);
                }
            }
        }).start();
    }
}