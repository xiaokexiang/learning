package top.leejay.interview.question9;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 * 模拟蛋糕师傅一直制作蛋糕并放到桌子上
 */
public class MakerThread extends Thread {
    private final Random random;
    private final Table table;
    /**
     * 模拟蛋糕的流水号，所有的厨师公用 synchronized增加流水号
     */
    private static int id = 0;

    public MakerThread(Table table, String name) {
        super(name);
        this.table = table;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(random.nextInt(1000));
                String cake = "[ Cake No." + nextId() + " by " + getName() + " ]";
                table.put(cake);
            }
        } catch (InterruptedException e) {

        }
    }

    private synchronized int nextId() {
        return id++;
    }
}
