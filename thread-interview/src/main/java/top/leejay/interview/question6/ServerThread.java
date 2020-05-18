package top.leejay.interview.question6;

import lombok.SneakyThrows;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * 读取RequestQueue中的Request实例
 */
public class ServerThread extends Thread {

    private final Random random = new Random();
    private final RequestQueue requestQueue;

    public ServerThread(RequestQueue requestQueue, String name) {
        super(name);
        this.requestQueue = requestQueue;
    }

    @SneakyThrows
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10000; i++) {
                Request request = (Request) requestQueue.getRequest();
                System.out.println(Thread.currentThread().getName() + " get: " + request);
                Thread.sleep(random.nextInt(1000));
            }
            // 不忽略打断异常 忽略会导致无法打断
        } catch (InterruptedException e) {
            System.out.println("server interrupt ....");
        }

    }
}
