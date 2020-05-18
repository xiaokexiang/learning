package top.leejay.interview.question13;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 针对一个请求另外创建一个线程去处理，类似异步async。如果一直是同一个线程处理那么就是同步sync
 * 下文的异步方法适合没有返回值的，如果需要有返回值的推荐Future<E> Callable
 * 同时该方法适合用于web服务器，接受request，处理交给其他线程。
 */
@SuppressWarnings("all")
public class Host {
    private final Helper helper = new Helper();

    public void request(final int count, final char c) {
        System.out.println(" request(" + count + ", " + c + ") BEGIN ... ");
        new Thread(() -> {
            helper.handler(count, c);
        }).start();
        System.out.println(" request(" + count + ", " + c + ") END ... ");
    }
}

class Helper {
    public void handler(int count, char c) {
        System.out.println("handle BEGIN ...");
        for (int i = 0; i < count; i++) {
            slowly();
            System.out.println(c);
        }
        System.out.println("handle END ...");
    }

    private void slowly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

}

class Main {
    public static void main(String[] args) {
        new Host().request(10, 'a');
    }
}