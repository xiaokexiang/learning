package top.leejay.interview.question11;

/**
 * @author xiaokexiang
 * @date 3/26/2020
 * 列举concurrent包中的BlockingQueue阻塞队列(在达到合适的状态之前一直阻塞的队列)及其实现类
 * <p>
 * 1. ArrayBlockingQueue 基于数组的BlockingQueue 元素个数有最大限制的BlockingQueue
 * 2. LinkedBlockingQueue 基于链表的BlockingQueue 元素个数没有最大限制的BlockingQueue
 * 3. PriorityBlockingQueue 带有优先级的BlockingQueue 数据的优先级是由comparable接口或者comparator接口决定的顺序决定的
 * 4. DelayQueue Delayed对象构成的BlockingQueue，一定时间之后才能够take
 * 5. SynchronousQueue 直接传递的BlockingQueue 如果p角色先put，在c角色take之前，p会一直阻塞。相反如果c先take，在p角色put之前，c会一直阻塞
 * 6. ConcurrentLinkedQueue 元素个数没有最大限制的线程安全队列
 * 7. java.util.concurrent.Exchanger 让两个线程安全的交换数据
 */
public class Queue {
}
