package top.leejay.interview.question15;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 * 负责传递工作请求以及保存工人线程的类
 */
public class Channel {
    private static final int MAX_REQUEST = 100;
    private final Request[] requestQueue;
    private int tail;
    private int head;
    private int count;

    private final WorkerThread[] workerThreads;

    public Channel(int threads) {
        this.requestQueue = new Request[MAX_REQUEST];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        workerThreads = new WorkerThread[threads];
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i] = new WorkerThread("Worker-" + i, this);
        }
    }

    public void startWorkers() {
        for (int i = 0; i < workerThreads.length; i++) {
            workerThreads[i].start();
        }
    }

    public synchronized void putRequest(Request request) {
        while (count >= requestQueue.length) {
            try {
                wait(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }

        requestQueue[tail] = request;
        tail = (tail + 1) % requestQueue.length;
        count++;
        notifyAll();
    }

    public synchronized Request takeRequest() {
        while (count <= 0) {
            try {
                wait(new Random().nextInt(1000));
            } catch (InterruptedException e) {
            }
        }
        Request request = requestQueue[head];
        head = (head + 1) % requestQueue.length;
        count--;
        notifyAll();
        return request;
    }
}
