package top.leejay.interview.question18;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
@SuppressWarnings("all")
public class InterruptOne {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
            }
            System.out.println(Thread.currentThread().getName() + ": " + Thread.interrupted()); // true
            System.out.println(Thread.currentThread().getName() + ": " + Thread.interrupted()); // false
        });

        thread.start();
        // 中断线程
        thread.interrupt();
        System.out.println(thread.isInterrupted());
        System.out.println(thread.isInterrupted());

        // 判断当前线程(main)是否中断并清除标志
        System.out.println(Thread.interrupted()); // false
        System.out.println(Thread.interrupted()); // false
    }
}
