package top.leejay.interview.question8;

import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 */
@SuppressWarnings("all")
public class HostTest {
    public static void main(String[] args) {
        Host host = new Host(5000);
        new Thread(() -> {
            try {
                host.execute();
            } catch (TimeoutException e) {
                // 抛出异常表明在timeout内没有修改状态 wait时间结束后抛出异常
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                // sleep time > timeout 会超时 否则会修改状态不会超时
                Thread.sleep(new Random().nextInt(10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            host.setExecutable(true);
        }).start();
    }
}
