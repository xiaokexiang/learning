package top.leejay.interview.question9;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 */
public class EaterThread extends Thread {
    private final Random random;
    private final Table table;

    public EaterThread(Table table, String name) {
        super(name);
        this.table = table;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (true) {
                String cake = table.take();
                System.out.println(Thread.currentThread().getName() + " takes " + cake);
                Thread.sleep(random.nextInt(1000));
            }
        } catch (InterruptedException e) {

        }

    }
}
