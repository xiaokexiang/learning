package top.leejay.design.singleton;

/**
 * @author xiaokexiang
 * @date 5/18/2020
 * 双重确认singleton
 * 饿汉和懒汉单例都存在缺点，如果为了保证多线程情况下单例的一致性，我们基于懒汉式单例创建双重确认单例。
 */
public class DoubleCheckSingleton {

    /**
     * 为什么这个地方要使用volatile修饰?
     *
     * 首先我们需要了解JVM是存在`编译器优化重排`功能的(编译器在不改变单线程语义情况下，重新安排代码的执行顺序。但是不保证多线程情况)
     * 跳到41行 执行如下代码
     * singleton = new DoubleCheckSingleton();
     * 在JVM是分成三步的：
     * 1. 开辟空间分配内存
     * 2. 初始化对象
     * 3. 创建指针指向分配的内存地址
     *
     * 在不使用volatile时，可能被JVM优化成
     * 1. 开辟空间分配内存
     * 3. 创建指针指向分配的内存地址
     * 2. 初始化对象
     *
     * 那么当线程A执行1&3步的时候，线程B获取了CPU执行权，去45行验证`null == singleton`，
     * 发现不为null，直接返回一个未初始化完成的singleton，导致程序出错。
     *
     * 所以volatile相比synchronized除了在
     * 内存可见性(指的是当一个线程对volatile修饰的变量进行写操作,JMM会立刻将该线程对应的本地内存中的共享变量的值刷新到主内存中，
     * 当一个线程对volatile修饰的变量进行读操作时，JMM会立刻将本地内存置为无效，从内存中读取共享变量的值)
     * 上存在相同的想过外，还严格限制编译器和处理器对 volatile变量 与 普通变量的重排序
     *
     * volatile禁止被修饰变量的 编译器重排序 和 处理器重排序(内存屏障) （JDK1.5后）
     *
     */
    private static volatile DoubleCheckSingleton singleton;

    private DoubleCheckSingleton() {
    }

    public static DoubleCheckSingleton getInstance() {
        // 不是任何线程进来都尝试去获取锁，而是先判断singleton是否为null，优化性能
        if (null == singleton) {
            // 尝试去获取锁，保证线程安全
            synchronized (DoubleCheckSingleton.class) {
                // 获取锁后判断singleton是否为null
                if (null == singleton) {
                    singleton = new DoubleCheckSingleton();
                }
            }
        }
        return singleton;
    }
}
