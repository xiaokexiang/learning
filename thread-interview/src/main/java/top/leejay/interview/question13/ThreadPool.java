package top.leejay.interview.question13;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @author xiaokexiang
 * @date 3/27/2020
 * 线程池的创建以及线程数量的选择
 * <p>
 * 1. corePoolSize：默认存活，即使空闲也不受超时时间限制
 * NThreads = NCpus * UCpus * (1 + W/C);
 * NThreads：线程数量
 * NCpus：cpu核数(Runtime.getRuntime().availableProcessors()可计算)
 * UCpus: cpu使用率(0~1)
 * W/C： wait time & compute time cpu运行类型分为I/O密集型和计算密集型分别对应W和C
 * <p>
 * 假设CPU使用率100%，那么NThreads = NCpus * (1 + W/C);
 * 如果是I/O密集型(数据库交互、文件上传下载、网络数据传输等)那么 W/C > 1
 * NThreads >= 2 * NCpus，具体项目还是要多测试，不想测试就取最小值2NCpus
 * <p>
 * 如果是计算密集型(复杂算法之类的)，W 接近于0 NThreads >= NCpus，推荐NCpus+1，这样即使当计算密集型线程偶尔由于缺失故障或者其他原因线程暂停，
 * 这个额外的线程也能确保CPU始终周期不被浪费， 至于多一个cpu上下文切换是否值得，具体项目具体测试。
 * 所以得出结论推荐： I/O密集型： 2NCpu；计算密集型 NCpus + 1
 * 公式来源： Java并发编程实战。
 * <p>
 * 2. maximumPoolSize
 * 线程池最大线程数量，一般是corePoolSize的2倍
 * <p>
 * 3. keepAliveTime:
 * 非core thread闲置的时候，超过这个时间就会被回收
 * <p>
 * 4. unit：超时单位
 * TimeUnit.Seconds
 * <p>
 * 5. workQueue: 阻塞队列的BlockingQueue<Runnable>实现类 用于缓存task
 * <p>
 * 6. ThreadFactory: 设置线程名称、类型等属性
 * <p>
 * 7. RejectedExecutionHandler： 拒绝策略
 * <p>
 * ThreadPoolExecutor.AbortPolicy()： 抛出java.util.concurrent.RejectedExecutionException异常
 * ThreadPoolExecutor.CallerRunsPolicy(): 直接由提交任务者执行这个任务
 * ThreadPoolExecutor.DiscardOldestPolicy(): 丢弃执行队列中最老的任务，尝试为当前提交的任务腾出位置
 * ThreadPoolExecutor.DiscardPolicy(): 什么也不做，直接忽略
 * <p>
 * 8. 添加任务到线程池
 * 1. 支持Runnable 或 Future任务
 * 2. 当一个任务添加到线程池的时候，如果此时线程池内线程数量小于corePoolSize，即使已存在的线程处于空闲，线程池也会创建新的线程来处理被添加的任务
 * 3. 如果此时线程池中线程数量等于corePoolSize，但是缓冲队列queue未满，任务将会放入缓冲队列
 * 4. 如果此时线程池中线程数量大于corePoolSize，缓冲队列queue满，并且线程池线程数量小于maximumPoolSize，创建新的线程处理该任务。
 * 5. 如果此时线程池中的数量大于corePoolSize，缓冲队列queue满，并且线程池中线程数量等于maximumPoolSize，那么通过handler所指定的策略来处理此任务。
 * 6. 当线程数量大于corePoolSize时，某线程空闲时间超过keepAliveTime，线程将被终止。
 * <p>
 * 9. 线程池适合单个处理任务时间比较短，或需要处理的任务数量大。
 */
public class ThreadPool {
    public static void main(String[] args) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,
                4,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                new CustomizableThreadFactory("Thread-pool-"),
//                new ThreadFactoryBuilder().setNameFormat("Demo-pool-%d").build(),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        // 提交20个任务
        for (int i = 0; i < 20; i++) {
            threadPoolExecutor.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " do something ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPoolExecutor.shutdown();

        // 创建定时线程池
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2,
                new CustomizableThreadFactory("Thread-pool-"),
                new ThreadPoolExecutor.AbortPolicy());

        // 1s后执行task
        ScheduledFuture<?> schedule = executor.schedule(() -> {
            System.out.println("Hello world ...");
            Thread.sleep(new Random().nextInt(3000));
            return "yes";
        }, 1, TimeUnit.SECONDS);

        try {
            // 获取task 结果
            String string = (String) schedule.get();
            System.out.println(string);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // 1s后每隔3s执行task，除非手动关闭否则不停止
        executor.scheduleAtFixedRate(() -> {
            System.out.println("hello ya ~");
        }, 1, 3, TimeUnit.SECONDS);

        executor.shutdown();
    }
}