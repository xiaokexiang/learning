package top.leejay.interview.question23;

import java.util.concurrent.Callable;

/**
 * @author xiaokexiang
 * @date 7/16/2020
 */
public class CallableTest {

    public static void main(String[] args) throws Exception {
        Callable<String> callable = () -> {
            System.out.println(Thread.currentThread().getName());
            Thread.sleep(20000);
            return "hello world";
        };

        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
            try {
                callable.call();
            } catch (Exception e) {
               e.printStackTrace();
            }
        });
        thread.start();
        thread.interrupt();
    }
}
