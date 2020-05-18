package top.leejay.interview.question4;

/**
 * @author xiaokexiang
 * @date 3/22/2020
 * 信号量Semaphore： 用于管制M个线程请求N个(M>N)资源出现的资源竞争。
 * permits(资源许可个数)：由Semaphore构造函数决定
 * acquire()：存在资源则立即返回，否则该线程阻塞在方法内，直到出现可用资源
 * release()：用于释放资源
 */
@SuppressWarnings("all")
public class Semaphores {
    public static void main(String[] args) {
        BoundedResources boundedResources = new BoundedResources(1);

        for (int i = 0; i < 2; i++) {
            // 启动线程
            new Thread(new UserThread(boundedResources)).start();
        }
    }
}
