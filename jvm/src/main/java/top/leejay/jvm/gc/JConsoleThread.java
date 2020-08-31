package top.leejay.jvm.gc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author xiaokexiang
 */
public class JConsoleThread {
    /**
     * 模拟 IO 等待、线程死循环、线程锁等待的状态
     */
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        bufferedReader.readLine();
        createBusyThread();
        bufferedReader.readLine();
        createLockThread();
    }

    static void createBusyThread() {
        new Thread(() -> {
            while (true) {
                // do something forever
            }
        }, "busy_thread").start();
    }

    private static final Object LOCK = new Object();
    static void createLockThread() {
        new Thread(() -> {
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "lock_thread").start();
    }
}
