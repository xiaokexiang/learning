package top.leejay.interview.question6;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 * 采用阻塞队列替换linkedList集合特性
 */
public class RequestQueue2 implements RequestQueue<Request> {

    /**
     * 线程安全的队列
     */
    private final BlockingQueue<Request> queue = new LinkedBlockingQueue<>();

    @Override
    public Request getRequest() {
        Request request = null;
        try {
            // take() 返回队首元素 当队列为空时调用take，则会wait()
            // 已实现互斥处理(ReentrantLock)
            request = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return request;
    }

    @Override
    public void putRequest(Request request) {
        try {
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
