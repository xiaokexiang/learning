package top.leejay.interview.question6;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * 用于存放 Request
 * @see top.leejay.interview.question6.Request
 */
public class RequestQueue1 implements RequestQueue<Request> {
    /**
     * 线程不安全的队列，所以需要保证操作的安全性
     */
    private final Queue<Request> queue = new LinkedList<>();

    @Override
    public synchronized Request getRequest() {
        // 注意这里是 while 不是 if 因为需要一致判断queue队列是否为空
        while (null == queue.peek()) {
            try {
                System.out.println(Thread.currentThread().getName() + " wait because queue is empty ...");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // return the head of queue
        return queue.remove();
    }

    @Override
    public synchronized void putRequest(Request request) {
        queue.offer(request);
        this.notifyAll();
    }
}
