package top.leejay.interview.question23;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xiaokexiang
 * @date 7/9/2020
 */
@Slf4j
public class CopyOnWriteArrayListTest {

    private static final CopyOnWriteArrayList<Integer> LIST = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LIST.add(2);
            LIST.add(3);
            LIST.add(5);
            LIST.add(7);
            LIST.add(9);
            log.info("add success ...");
        }).start();

        new Thread(() -> {
            log.info("index 0: {}", !LIST.isEmpty() ? LIST.get(0) : null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("index 0: {}", LIST.get(0));
        });
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            Integer remove = LIST.remove(2);
            System.out.println(remove);
        }).start();
    }
}
