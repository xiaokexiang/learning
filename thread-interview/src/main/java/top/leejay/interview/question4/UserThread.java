package top.leejay.interview.question4;

import lombok.SneakyThrows;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/22/2020
 * 模拟M个使用资源线程
 */
public class UserThread implements Runnable {

    private final BoundedResources resources;

    public UserThread(BoundedResources resources) {
        this.resources = resources;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            resources.use();
            Thread.sleep(new Random().nextInt(3000));
        }
    }
}
