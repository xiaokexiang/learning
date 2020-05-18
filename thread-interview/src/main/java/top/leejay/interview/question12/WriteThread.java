package top.leejay.interview.question12;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 该类不存在互斥代码，由Data中read-write lock 实现
 */
public class WriteThread extends Thread {
    private static final Random RANDOM = new Random();
    private final Data data;
    private final String filter;
    private int index = 0;

    public WriteThread(String name, Data data, String filter) {
        super(name);
        this.data = data;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            while (true) {
                char c = nextChar();
                data.write(c);
                Thread.sleep(RANDOM.nextInt(3000));
            }
        } catch (InterruptedException e) {
        }
    }

    private char nextChar() {
        char c = filter.charAt(index);
        index++;
        if (index >= filter.length()) {
            index = 0;
        }
        return c;
    }
}
