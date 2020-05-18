package top.leejay.interview.question8;

import java.util.concurrent.TimeoutException;

/**
 * @author xiaokexiang
 * @date 3/25/2020
 * 实现带有超时功能的guarded suspension线程设计
 * wait(指定时长)后抛出异常用于表示wait结束
 */
public class Host {
    /**
     * 超时时长
     */
    private final long timeout;
    /**
     * false 为未准备好
     */
    private boolean ready = false;

    public Host(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 修改状态用于不超时
     */
    public synchronized void setExecutable(boolean on) {
        ready = true;
        notifyAll();
    }

    public synchronized void execute() throws TimeoutException, InterruptedException {
        long start = System.currentTimeMillis();
        while (!ready) {
            long now = System.currentTimeMillis();
            // 计算需要wait的时长
            long rest = timeout - (now - start);
            // 如果rest小于0 说明在timeout期限内，ready状态没有被修改
            if (rest < 0) {
                throw new TimeoutException("now - start: " + (now - start) + " timeout: " + timeout);
            }
            // 等待rest后继续进 while判定条件
            wait(rest);
        }
        doExecute();
    }

    private void doExecute() {
        System.out.println(Thread.currentThread().getName() + " call doExecute ... ");
    }
}
