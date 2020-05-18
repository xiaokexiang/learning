package top.leejay.interview.question17;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 */
@SuppressWarnings("all")
public class HostFuture {
    public Data request(final int count, final char c) {
        // 传入Callable实现类
        FutureTaskData futureTaskData = new FutureTaskData(() -> new RealData(count, c));
        // 传入FutureTask子类 启动线程
        new Thread(futureTaskData).start();
        return futureTaskData;
    }
}
