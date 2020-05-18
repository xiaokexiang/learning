package top.leejay.interview.question9;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 */
public class Main {
    public static void main(String[] args) {
        Table table = new Table(3);
        new MakerThread(table, "Maker-one").start();
        new MakerThread(table, "Maker-two").start();
        new MakerThread(table, "Maker-three").start();
        new EaterThread(table, "Eater-one").start();
        new EaterThread(table, "Eater-two").start();
    }
}
