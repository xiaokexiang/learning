package top.leejay.interview.question18;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
public class CountUpThread extends Thread {

    private long counter = 0;
    private volatile boolean shutdownRequested = false;

    public void shutdownRequest() {
        // 修改状态
        shutdownRequested = true;
        // main线程打断线程
        this.interrupt();
    }

    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    @Override
    public void run() {
        try {
            while (!isShutdownRequested()) {
                doWork();
            }
        } catch (InterruptedException e) {
            System.out.println("thread interrupt ...");
        } finally {
            doShutDown();
        }
    }

    private void doShutDown() {
        System.out.println("doShutDown: counter = " + counter);
    }

    private void doWork() throws InterruptedException {
        counter++;
        System.out.println("doWork: counter = " + counter);
        // 会在这里查询是否被打断，是就抛出异常
        Thread.sleep(500);
    }

}
