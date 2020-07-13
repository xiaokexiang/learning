package top.leejay.interview.question23;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaokexiang
 * @date 7/13/2020
 */
public class ConcurrentHashMapTest {

    public static void main(String[] args) {

        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();

        new Thread(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            concurrentHashMap.put("hello", "world");
        }).start();

        new Thread(() -> {
            System.out.println(concurrentHashMap.get("hello"));
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(concurrentHashMap.get("hello"));
        }).start();


    }
}
