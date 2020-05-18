package top.leejay.interview.question6;

import lombok.SneakyThrows;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 */
public class ClientThread extends Thread {

    private final Random random = new Random();
    private final RequestQueue requestQueue;

    public ClientThread(RequestQueue requestQueue, String name) {
        super(name);
        this.requestQueue = requestQueue;
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        // 循环产生10000个Request对象插入到队列中
        try {
            for (int i = 0; i < 10000; i++) {
                Request request = new Request("request " + i);
                System.out.println(Thread.currentThread().getName() + " put: " + request);
                requestQueue.putRequest(request);
                Thread.sleep(random.nextInt(1000));
            }

            // 不忽略打断异常
        } catch (InterruptedException e) {
            System.out.println("client interrupt ....");
        }
    }
}

