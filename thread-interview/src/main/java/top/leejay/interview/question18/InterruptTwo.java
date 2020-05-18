package top.leejay.interview.question18;

import lombok.SneakyThrows;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
@SuppressWarnings("all")
public class InterruptTwo {
    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // 只要是进入catch部分，那么中断状态肯定已经清除了
                System.out.println(Thread.currentThread().isInterrupted());// false
                // 补上打断状态
                Thread.currentThread().interrupt();
            }

            // 此时中断状态为true
            if (Thread.currentThread().isInterrupted()) {
                System.out.println(Thread.currentThread().getName() + " is interrupt ...");
            }
        });

        thread.start();
        Thread.sleep(2000);
        thread.interrupt();
    }
}
