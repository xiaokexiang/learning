package top.leejay.interview.question8;

import lombok.SneakyThrows;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 */
@SuppressWarnings("all")
public class DataTest {
    @SneakyThrows
    public static void main(String[] args) {
        Data data = new Data("hello.txt", "hello world", false);
        new ChangeThread(data, "change").start();
        Thread.sleep(1000);
        new SaveThread(data, "save").start();
    }
}
