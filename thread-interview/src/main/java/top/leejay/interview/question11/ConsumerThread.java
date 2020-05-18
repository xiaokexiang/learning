package top.leejay.interview.question11;

import java.util.Random;
import java.util.concurrent.Exchanger;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 接受producer的数据
 */
public class ConsumerThread extends Thread {
    private final Exchanger<char[]> exchanger;
    private char[] buffer = null;
    private final Random random;

    public ConsumerThread(String name, Exchanger<char[]> exchanger, char[] buffer) {
        super(name);
        this.exchanger = exchanger;
        this.buffer = buffer;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println(Thread.currentThread().getName() + " before exchange ... ");
                // 获取其他线程的buffer，将当前线程的buffer用于交换
                buffer = exchanger.exchange(buffer);
                System.out.println(Thread.currentThread().getName() + " after exchange");
                for (int i = 0; i < buffer.length; i++) {
                    System.out.println(Thread.currentThread().getName() + ": " + " -> " + buffer[i]);
                    Thread.sleep(random.nextInt(1000));
                }
            }
        } catch (InterruptedException e) {

        }
    }
}
