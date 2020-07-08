package top.leejay.interview.question23;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @author xiaokexiang
 * @date 7/8/2020
 */
public class BlockingQueueTest {
    private static final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(2 << 32);
    private static final SynchronousQueue<Integer> SYNCHRONOUS_QUEUE = new SynchronousQueue<>();
    public static void main(String[] args) {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    queue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        new Thread(() -> {
            while (true) {
                try {
                    System.out.println(queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        new Thread(() -> {
//            try {
//                SYNCHRONOUS_QUEUE.put(123);
//                System.out.println("waiting ...");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//        new Thread(() -> {
//            try {
//                System.out.println("sleeping ...");
//                Thread.sleep(2000L);
//                System.out.println(SYNCHRONOUS_QUEUE.take());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }
}
