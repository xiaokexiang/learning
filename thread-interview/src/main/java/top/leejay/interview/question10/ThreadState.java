package top.leejay.interview.question10;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * synchronized： 1. 每个实例都拥有一个独立的锁，并不是某一个实例的synchronized方法正在执行，其他实例中synchronized方法就不能运行的
 *                2. 如果一个实例中拥有多个synchronized方法，某个线程已经持有了synchronized锁进入方法A，那么其他线程也无法进入方法B，
 *                因为同一个实例中synchronized持有的锁就是实例本身(静态锁就是实例的class对象)。
 *
 * 线程的状态转换： http://image.leejay.top/image/20200326/3XSAP42BEbCV.png
 */
public class ThreadState {
}
