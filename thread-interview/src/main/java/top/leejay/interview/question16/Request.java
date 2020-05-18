package top.leejay.interview.question16;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
public class Request implements Runnable {
    private final String name;
    private final int number;
    private static final Random RANDOM = new Random();

    public Request(String name, int number) {
        this.name = name;
        this.number = number;
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                ", number=" + number +
                '}';
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " executes " + this);
        try {
            Thread.sleep(RANDOM.nextInt(1000));
        } catch (InterruptedException e) {
        }
    }
}
