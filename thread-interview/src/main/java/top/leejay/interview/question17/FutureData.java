package top.leejay.interview.question17;

import java.util.concurrent.ExecutionException;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 * 模拟实现Future功能，没有结果的时候来获取就等待，有结果就返回结果
 * 支持抛出异常
 */
public class FutureData implements Data {
    /**
     * 需要拿去的货物
     */
    private RealData realData;
    /**
     * 是否能够拿取
     */
    private boolean ready = false;

    /**
     * 用于抛出的异常
     */
    private ExecutionException exception = null;


    /**
     * 该方法的作用其实就是修改ready状态，这样在getContent的时候能够不wait
     */
    synchronized void setRealData(RealData realData) {
        if (ready) {
            return;
        }
        this.realData = realData;
        this.ready = true;
        notifyAll();
    }

    synchronized void setException(Throwable throwable) {
        // 如果ready为true就不能进入该方法了
        if (ready) {
            return;
        }
        this.exception = new ExecutionException(throwable);
        this.ready = true;
        notifyAll();
    }


    @Override
    public String getContent() throws ExecutionException {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 如果异常不为null就抛出该异常
        if (null != exception) {
            throw exception;
        }
        return realData.getContent();
    }
}
