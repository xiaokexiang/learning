package top.leejay.interview.question17;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
@SuppressWarnings("all")
public class Host {
    public Data request(final int count, final char c) {
        final FutureData future = new FutureData();
        new Thread(() -> {
            try {
                RealData realData = new RealData(count, c);
                future.setRealData(realData);
            } catch (Exception e) {
                future.setException(e.getCause());
            }
        }).start();
        return future;
    }
}
