package top.leejay.interview.question10;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 模拟Thread.sleep功能
 */
public class CustomSleep {
    public static void method(long time) throws InterruptedException {
        if (time != 0) {
            // 因为是在方法内部创建了锁的实例，无法从外部获取该锁从而notify/interrupt该线程，所以作用类似Thread.sleep()
            Object object = new Object();
            synchronized (object) {
                object.wait(time);
            }
        }
    }


}

