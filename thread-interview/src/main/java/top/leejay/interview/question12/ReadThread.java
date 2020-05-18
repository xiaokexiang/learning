package top.leejay.interview.question12;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 该类不存在互斥代码，由Data中read-write lock 实现
 */
public class ReadThread extends Thread {
    private final Data data;

    public ReadThread(String name, Data data) {
        super(name);
        this.data = data;
    }

    @Override
    public void run() {
        try {
            while (true) {
                char[] readBuf = data.read();
                System.out.println(Thread.currentThread().getName() + " reads " + String.valueOf(readBuf));
            }
        } catch (InterruptedException e) {
        }
    }
}
