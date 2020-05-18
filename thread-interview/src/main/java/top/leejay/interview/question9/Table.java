package top.leejay.interview.question9;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 * 模拟桌子，类似管道 厨师放 客户拿
 */
public class Table {

    private final Queue<String> queue;
    private final Random random = new Random();

    /**
     * queue中的最多蛋糕个数
     */
    private int size;

    /**
     * 桌子能放几个蛋糕
     *
     * @param size 蛋糕数量
     */
    public Table(int size) {
        this.queue = new LinkedBlockingQueue<>();
        this.size = size;
    }

    public synchronized void put(String cake) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " puts " + cake);
        // 如果当前蛋糕个数大于等于最大能放个数，就不能再放
        while (queue.size() >= size) {
            wait(random.nextInt(1000));
        }
        queue.offer(cake);
    }

    public synchronized String take() throws InterruptedException {
        while (queue.size() <= 0) {
            wait(random.nextInt(1000));
        }
        String cake = queue.remove();
        return cake;
    }
}
