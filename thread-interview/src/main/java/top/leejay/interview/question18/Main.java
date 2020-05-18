package top.leejay.interview.question18;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class Main {
    public static void main(String[] args) {
        try {
            CountUpThread thread = new CountUpThread();
            thread.start();
            Thread.sleep(10000);

            System.out.println("prepare to shutDown request ...");
            thread.shutdownRequest();

            // 等待thread线程停止
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
