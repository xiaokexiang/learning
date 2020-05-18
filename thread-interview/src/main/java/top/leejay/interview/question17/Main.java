package top.leejay.interview.question17;

import lombok.SneakyThrows;

import java.util.concurrent.ExecutionException;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        // custom future
        Host host = new Host();
        Data a = host.request(10, 'a');
        Thread.sleep(2000);
        try {
            System.out.println(a.getContent());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Thread.sleep(2000);

        // future task
        HostFuture hostFuture = new HostFuture();
        Data b = hostFuture.request(5, 'B');

        try {
            System.out.println(b.getContent());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
