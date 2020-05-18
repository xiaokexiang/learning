package top.leejay.interview.question16;

import java.util.Random;
import java.util.concurrent.ExecutorService;


/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
public class ClientThread extends Thread {

    /**
     * 注入线程池
     */
    private final ExecutorService executorService;
    private static final Random RANDOM = new Random();

    public ClientThread(String name, ExecutorService executorService) {
        super(name);
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; true; i++) {
                Request request = new Request(getName(), i);
                executorService.execute(request);
                Thread.sleep(RANDOM.nextInt(1000));
            }
        } catch (InterruptedException e) {
        }
    }
}
