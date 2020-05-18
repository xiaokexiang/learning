package top.leejay.interview.question7;

/**
 * @author xiaokexiang
 * @date 3/23/2020
 * 测试Thread.yield()
 * 使当前线程从执行状态变成可执行状态，cpu会从众多可执行态里选择(尽可能将线程优先级让给其他线程)
 * 也就是刚刚的那个线程还有可能继续执行到，但是并不是说一定会执行其他线程而不执行当前线程
 *
 * 注意：Thread.yield并不会释放锁 所以不能写在synchronized代码块中
 */
@SuppressWarnings("all")
public class ThreadYield {
    public static void main(String[] args) {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " prepare exectue ...");
            Thread.yield();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " exectue ...");
        }, "Thread-one").start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " exectue ...");
        }, "Thread-two").start();
    }
}
