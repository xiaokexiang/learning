package top.leejay.interview.question1;

/**
 * @author xiaokexiang
 * @date 1/7/2020
 * 交替打印0-100奇偶数: wait & notify 配合synchronized
 */
@SuppressWarnings("all")
public class Answer2 {
    private static final Object obj = new Object();
    private static Integer count = 0;

    public static void main(String[] args) {
        new Thread(() -> {
            while (count <= 100) {
                synchronized (obj) {
                    if (count % 2 == 1) {
                        System.out.println("奇数: " + count);
                        count++;
                        obj.notify();
                    } else {
                        try {
                            obj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "奇数线程").start();

        new Thread(() -> {
            while (count <= 100) {
                synchronized (obj) {
                    if (count % 2 == 0) {
                        System.out.println("偶数: " + count);
                        count++;
                        obj.notify();
                    } else {
                        try {
                            obj.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, "偶数线程").start();
    }
}
