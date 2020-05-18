package top.leejay.interview.question10;

import lombok.SneakyThrows;

import java.util.Random;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 线程中关于InterruptedException的处理
 * 如果一个方法后面加了throws InterruptedException,则表明该方法中(或者该方法进一步调用的方法中)可能会抛出该异常。
 * 可以理解成加了throws InterruptedException的方法可能会花费时间，但是可以取消。
 * <p>
 * 其中：
 * wait(等待被唤醒)、sleep(等待休眠结束)和join(等待指定线程执行完毕)会抛出该异常，这些就是花费时间的方法。但是同时这些是可以被终止的。
 * <p>
 * sleep & interrupt: 无论何时，任何线程都可以调用其他线程的interrupt方法。
 * <p>
 * wait & interrupt: 执行wait()等待时，调用interrupt会等到wait获取锁之后才会抛出异常。
 * <p>
 * notify & interrupt： 作用类似都是为了唤醒线程，但是notify唤醒的是实例等待队列中的线程，而不是指定线程。并且必须持有锁才能唤醒，且唤醒后
 * 从wait(）的下一句开始执行。 interrupt可以唤醒指定线程且不需要获取锁才能执行。
 * <p>
 * join & interrupt： 和sleep类似，调用join方法无需获取锁
 * <p>
 * tips: 对于sleep、join 和 wait 的interrupt存在不同的情况，原因是sleep和join的执行时候线程不需要获取锁，而wait需要获取锁，所以只有等到wait的线程重新
 * 持有锁之后才会抛出InterruptedException。
 * <p>
 * tips: 为什么wait、notify和notifyAll 是属于object的方法？因为这三个方法是用于操作实例的等待队列，且所有实例都有等待队列。所以成为了object的方法
 * <p>
 * interrupt()方法只是改变了中断状态(表示线程是否中断的状态)，只有当线程执行到sleep、wait、join等方法的时候才会去检查中断状态及是否抛出异常
 * (如果抛出异常，那么线程不会变成中断状态)。
 * 如果没有执行相关检查中断状态的代码，那么永远不会抛出InterruptException异常。
 * <p>
 * threadA.isInterrupted 判断某线程是否处于中断装填 true/false
 * Thread.interrupted 检查并清除中断状态(只能清除当前线程的中断状态，不能清除其他线程的)，返回tru/false
 * threadA.interrupt 中断指定线程会有两个结果： 1.线程变成中断状态 2. 抛出中断异常但不会改变中断状态(wait、sleep、join)
 */
@SuppressWarnings("all")
public class InterruptedExceptions {
    public static void main(String[] args) {
        /*sleep interrupt*/
        Thread threadA = new Thread(() -> {
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                System.out.println("threadA sleep 被取消了。。。");
            }
        });
        threadA.start();

        // 可以用main线程打断，也可以新启线程去打断
        new Thread(() -> {
            // 用于打断上面的线程
            threadA.interrupt();
        }).start();

        /*wait interrupt*/
        Condition condition = new Condition();
        Thread threadB = new Thread(() -> {
            try {
                condition.print();
            } catch (InterruptedException e) {
                System.out.println("wait interrupt ...");
            }
        });
        threadB.start();

        new Thread(() -> {
            try {
                // 随机时长后再去打断threadB
                Thread.sleep(new Random().nextInt(10000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadB.interrupt();
        }).start();

        /*join interrupt*/
        Thread threadC = new Thread(() -> {

            for (int i = 0; i < 100; i++) {
                System.out.println("thread c ...");
            }
        });
        threadC.start();

        Thread threadD = new Thread(() -> {
            try {
                threadC.join();
            } catch (InterruptedException e) {
                System.out.println("join interrupt ...");
            }
            System.out.println("thread c complete ...");
        });
        threadD.start();

        new Thread(() -> {
            threadD.interrupt();
        }).start();
    }
}

class Condition {
    private boolean flag;

    public Condition() {
        this.flag = false;
    }

    public synchronized void print() throws InterruptedException {
        while (!flag) {
            System.out.println(Thread.currentThread().getName() + " wait ...");
            wait();
        }
        System.out.println("print ...");
        flag = false;
    }

    public synchronized void change() {
        flag = true;
        notifyAll();
    }
}

@SuppressWarnings("all")
class InterruptTest {
    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                // 判断当前是否中断(不会重置状态)
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("interrupte true ... ");
                    Thread.interrupted();// 重置interrupt状态
                } else {
                    System.out.println("interrupte false ...");
                }
            }
        });
        thread.start();
        // 中断指定线程
        thread.interrupt();
    }
}
