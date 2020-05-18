package top.leejay.interview.question18;

/**
 * @author xiaokexiang
 * @date 3/29/2020
 * 未捕获异常处理器&JVM退出钩子&线程的执行顺序
 */
@SuppressWarnings("all")
public class ProgramExit {
    public static void main(String[] args) {

        System.out.println("main begin ...");
        // 1. 设置未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.out.println("UncaughtExceptionHandler begin ...");
            System.out.println("Thead: " + t.getName());
            System.out.println("Exception: " + e);
            System.out.println("UncaughtExceptionHandler end ...");
        });
        // 2. 添加JVM退出钩子
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("shutdown hook begin ...");
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("shutdown hook end ...");
                })
        );
        // 3. 3s后启动抛出异常的线程
        new Thread(() -> {
            System.out.println("MyThread begin ...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            int x = 1 / 0;
            System.out.println("MyThread end ...");
        }).start();

        System.out.println("main end ...");
    }
}
