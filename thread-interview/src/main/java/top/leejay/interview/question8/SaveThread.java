package top.leejay.interview.question8;

import java.io.IOException;
import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 * 保存内容线程
 */
public class SaveThread extends Thread {
    private final Data data;
    private final Random random = new Random();

    public SaveThread(Data data, String name) {
        super(name);
        this.data = data;
    }

    @Override
    public void run() {

        try {
            while (true) {
                data.save();
                // 这个sleep的时长很重要 为什么是随机数不是固定值，能够模拟出不同的sleep实现，不会让save或change线程一直单独执行
                Thread.sleep(random.nextInt(1000));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
