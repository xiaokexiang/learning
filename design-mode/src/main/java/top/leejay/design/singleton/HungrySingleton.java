package top.leejay.design.singleton;

/**
 * @author xiaokexiang
 * @date 5/18/2020
 * 饿汉式(不管你用不用都创建) 线程安全 类初始化的时候就创建实例放入静态方法区，保证了多线程情况下的数据一致性
 */
public class HungrySingleton {
    private static HungrySingleton singleton = new HungrySingleton();

    private HungrySingleton() {}

    public static HungrySingleton getInstance() {
        return singleton;
    }
}
