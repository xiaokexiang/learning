package top.leejay.interview.question11;

import java.util.Random;
import java.util.concurrent.Exchanger;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 接受consumer的空数据
 */
public class ProducerThread extends Thread {

    /**
     * 用于交换的缓冲区
     */
    private final Exchanger<char[]> exchanger;
    private char[] buffer = null;
    private char index = 0;
    private final Random random;

    public ProducerThread(String name, Exchanger<char[]> exchanger, char[] buffer) {
        super(name);
        this.exchanger = exchanger;
        this.buffer = buffer;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (true) {
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = nextChar();
                    System.out.println(Thread.currentThread().getName() + ": " + buffer[i] + " -> ");
                }
                System.out.println(Thread.currentThread().getName() + " before exchange ... ");
                // 获取其他线程的buffer，将当前线程的buffer用于交换
                buffer = exchanger.exchange(buffer);
                System.out.println(Thread.currentThread().getName() + " after exchange");
            }
        } catch (InterruptedException e) {
        }
    }

    private synchronized char nextChar() throws InterruptedException {
        char c = (char) ('A' + index % 26);
        index++;
        Thread.sleep(random.nextInt(1000));
        return c;
    }
}
