package top.leejay.interview.question6;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 * 线程能够被打断，需要抛出InterruptedException
 */
public class RequestQueue3 implements RequestQueue<Request> {

    private final BlockingQueue<Request> queue = new LinkedBlockingQueue<>();

    @Override
    public Request getRequest() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void putRequest(Request request) throws InterruptedException {
        queue.put(request);
    }
}
