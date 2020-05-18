package top.leejay.interview.question8;

import lombok.SneakyThrows;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 * 修改内容线程
 */
public class ChangeThread extends Thread {
    private final Data data;
    private final Random random = new Random();

    public ChangeThread(Data data, String name) {
        super(name);
        this.data = data;
    }

    @SneakyThrows
    @Override
    public void run() {
        for (int i = 0; true; i++) {
            // 修改文件
            data.change("No." + i + "\r\n");
            // 模拟执行其他操作
            Thread.sleep(random.nextInt(1000));
            // 保存文件
            data.save();
        }
    }
}
