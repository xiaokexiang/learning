package top.leejay.interview.question10;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 为什么推荐使用notifyAll 而不是 notify
 * 1. 首先notify相比notifyAll执行时间更短
 * 2. 但是notify只能唤醒一个线程，如果唤醒无关线程(业务无关)，那么可能导致已有业务逻辑无法执行
 */
@SuppressWarnings("all")
public class Notify {
    public static void main(String[] args) {
        Work work = new Work();
        new WorkThread("Thread-one", work).start();
        new WorkThread("Thread-two", work).start();
        new WorkThread("Thread-three", work).start();
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            work.change();
        }).start();
        new NoWorkThread("no-work-one", work).start();
        new NoWorkThread("no-work-two", work).start();
    }
}

class WorkThread extends Thread {

    private final Work work;

    public WorkThread(String name, Work work) {
        super(name);
        this.work = work;
    }

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " running ... ");
            work.print();
        } catch (InterruptedException e) {
        }
    }
}

/**
 * 只要是该线程获取锁都继续放入wait队列
 */
class NoWorkThread extends Thread {
    private final Work work;

    public NoWorkThread(String name, Work work) {
        super(name);
        this.work = work;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 获取work锁实例，因为是无关线程获取锁要让他继续休眠
                synchronized (work) {
                    // 如果能获取放入等待队列
                    work.wait();
                }
                System.out.println("no work wait ...");
            } catch (InterruptedException e) {
            }
        }
    }
}

class Work {
    private boolean flag;

    public Work() {
        this.flag = false;
    }

    public synchronized void print() throws InterruptedException {
        while (!flag) {
            wait();
        }
        System.out.println(Thread.currentThread().getName() + " print ...");
    }

    public synchronized void change() {
        flag = true;
        notifyAll();
    }
}
