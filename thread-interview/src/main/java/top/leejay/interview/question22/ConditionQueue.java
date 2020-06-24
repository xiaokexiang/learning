package top.leejay.interview.question22;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xiaokexiang
 * @date 6/23/2020
 * 使用condition模拟实现ArrayBlockingQueue的功能
 */
@Slf4j
public class ConditionQueue<T> {
    private final List<T> queue;
    private final int limit;
    private final ReentrantLock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    public ConditionQueue(int capacity, boolean fair) {
        this.lock = new ReentrantLock(fair);
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
        queue = new ArrayList<>();
        limit = capacity;
    }

    public void put(T t) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == limit) {
                // 将添加元素的线程加入notFull队列，等待下次唤醒再次put
                notFull.await();
            }
            queue.add(t);
            // 唤醒获取元素线程获取元素
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T poll() throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == 0) {
                // 将获取元素线程加入notEmpty队列，等待下次再次获取
                notEmpty.await();
            }
            // 唤醒添加元素线程添加元素
            notFull.signal();
            return queue.remove(0);
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    private static final ThreadPoolExecutor POOL = ThreadPoolSingleton.getInstance();

    public static void main(String[] args) {
        ConditionQueue<Integer> queue = new ConditionQueue<>(4, true);
        // 8个线程去put值
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            POOL.execute(() -> {
                try {
                    System.out.println("put " + finalI);
                    queue.put(finalI);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        // 获取值，值的顺序由put的顺序决定
        POOL.execute(() -> {
            while (!queue.isEmpty()) {
                try {
                    log.info("value: {}", queue.poll());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        POOL.shutdown();
    }
}
